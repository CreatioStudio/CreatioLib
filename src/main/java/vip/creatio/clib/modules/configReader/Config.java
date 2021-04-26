package vip.creatio.clib.modules.configReader;

import vip.creatio.common.StringUtil;
import org.apache.commons.lang3.StringEscapeUtils;

import java.util.*;

/**
 * A relatively independent yml reader, theoretically this can be used in any java projects.
 *
 * To make it fully work you need to include class com.henryrenyz.common.StringUtil.
 *
 * @see StringUtil
 * @author HenryRenYz
 */
public class Config {

    private final Map<String, ConfigBase> element;
    private List<String> attachedFooter;
    private int tab = 1;

    public Config(List<String> raw) {
        this.element = readMap(raw);
    }

    private Map<String, ConfigBase> readMap(List<String> item) {
        List<Map<String, List<?>>> l = analyze(item);
        for (Map<String, List<?>> c : l) {
            for (Map.Entry<String, List<?>> ent : c.entrySet()) {
                System.out.println(ent);
            }
        }
        synthesis(l);

        System.out.println("attachedFooter = " + attachedFooter);

        return null;
    }

    private List<Map<String, List<?>>> analyze(List<String> item) {

        List<Map<String, List<?>>> list = new ArrayList<>();
        List<String> comments = new ArrayList<>();
        List<String> value = new ArrayList<>();

        Map<String, List<?>> buffer = new HashMap<>();
        int blankRow = 0;


        boolean outside = false;
        String key = ">>none>>";

        for (String s : item) {
            int firstChar = StringUtil.matchAfter(s, "*any*");

            if (tab == 1 && !s.isEmpty()) tab = Math.max(firstChar, 1);

            if (!s.isEmpty()) outside = firstChar < tab && firstChar >= 0;

            if (outside) {
                if (!s.isEmpty()) {
                    if (s.charAt(firstChar) == '#') {
                        comments.add(s.trim());
                    } else {
                        if (firstChar == 0) {
                            if (!key.equals(">>none>>")) {
                                int cm = seekForComments(key);
                                if (cm != -1) {
                                    comments.add(key.substring(cm));
                                    key = key.substring(0, cm).trim();
                                }
                                buffer.put(key, (!value.isEmpty()) ? analyze(value) : null);
                                buffer.put(">>row>>", Collections.singletonList(blankRow));
                                blankRow = 0;
                                list.add(buffer);
                                buffer = new HashMap<>();
                            }
                            key = s.trim();
                            if (comments.size() != 0) {
                                buffer.put(">>comment>>", comments);
                                comments = new ArrayList<>();
                            }
                            value = new ArrayList<>();
                        }
                    }
                } else blankRow++;
            } else {
                if (s.trim().isEmpty()) {
                    value.add("");
                } else {
                    if (tab > 0) {
                        value.add(s.substring(tab));
                    }
                }
            }
        }
        if (!key.equals(">>none>>")) {
            buffer.put(key, (!value.isEmpty()) ? analyze(value) : null);
            buffer.put(">>row>>", Collections.singletonList(blankRow));
            list.add(buffer);
        }

        if (!comments.isEmpty()) {
            this.attachedFooter = comments;
        }
        return list;
    }

    private Map<String, ConfigBase> synthesis(List<Map<String, List<?>>> item) {
        Map<String, ConfigBase> m = new HashMap<>();
        for (Map<String, List<?>> map : item) {
            List<String> comments = new ArrayList<>();
            List<?> contents;
            for (Map.Entry<String, List<?>> entry : map.entrySet()) {
                if (entry.getKey().equals(">>comment>>")) {
                    comments = (List<String>) entry.getValue();
                } else {
                    contents = entry.getValue();
                    String[] sp = entry.getKey().trim().split(":", 2);
                    Object obj = "";
                    if (contents == null) {

                        String val;
                        if (sp.length == 1) val = "";
                        else val = sp[1].trim();

                        if (val.charAt(0) == '[') {
                            obj = StringUtil.parseList(val);
                        } else if (val.charAt(0) == '{') {
                            obj = StringUtil.parseMap(val);
                        } else {
                            //Number casting
                            try {
                                if (val.equalsIgnoreCase("true") || val.equalsIgnoreCase("false")) {
                                    obj = Boolean.parseBoolean(val);
                                } else {
                                    switch (val.charAt(val.length() - 1)) {
                                        case 'd':
                                        case 'D':
                                            obj = Double.parseDouble(val);
                                            break;
                                        case 'f':
                                        case 'F':
                                            obj = Float.parseFloat(val);
                                            break;
                                        case 's':
                                        case 'S':
                                            obj = Short.parseShort(val.substring(0, val.length() - 1));
                                            break;
                                        case 'l':
                                        case 'L':
                                            obj = Long.parseLong(val.substring(0, val.length() - 1));
                                            break;
                                        case 'b':
                                        case 'B':
                                            obj = Byte.parseByte(val.substring(0, val.length() - 1));
                                            break;
                                        case '"':
                                        case '\'':
                                            obj = parseString(val);
                                            break;
                                        default:
                                            obj = Integer.parseInt(val);
                                    }
                                }
                            } catch (NumberFormatException ignored) {}
                        }

                        //Fallback type: String
                        if (obj.equals("")) obj = parseString(val);
                        if (val.equalsIgnoreCase("null") || val.equalsIgnoreCase("~")) obj = null;
                    } else {
                        Object st = contents.get(0);
                        System.out.println(st);
                        if (st instanceof String) {
                            //List casting '-'
                            //if (StringUtil.matchAfter("- "))
                            obj = parseList(contents);

                            //List casting '|' & '>'
                        } else if (st instanceof Map) {

                        }
                    }

                    ConfigBase c = new ConfigElement(obj);
                    c.setComment(comments);
                    m.put(sp[0], c);
                }
            }
        }
        return m;
    }

    private static List<?> parseList(List<?> contents) {
        return null;
    }

    private static int seekForComments(String src) {
        boolean inside = false;
        boolean inside2 = false;
        boolean escaping = false;
        for (int i = 0; i < src.length(); i++) {
            switch (src.charAt(i)) {
                case '"':
                    if (!escaping) inside = !inside;
                    break;
                case '\'':
                    if (!escaping) inside2 = !inside2;
                    break;
                case '\\':
                    if (inside) escaping = !escaping;
                    break;
                case '#':
                    if (!inside && !inside2 && !escaping) return i;
                    break;
                default:
                    escaping = false;
            }
        }
        return -1;
    }

    private static String parseString(String src) {
        src = src.trim();
        if (src.charAt(0) == '"'&& src.charAt(src.length() - 1) == '"') {
            return StringEscapeUtils.unescapeJava(src.substring(1, src.length() - 1));
        }
        if (src.charAt(0) == '\'' && src.charAt(src.length() - 1) == '\'') {
            return src.substring(1, src.length() - 1);
        }
        return src;
    }

    public Map<String, ConfigBase> getElement() {
        return element;
    }
}
