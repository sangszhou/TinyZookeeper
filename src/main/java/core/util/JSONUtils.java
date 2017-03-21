package core.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;

/**
 * Created by xinszhou on 3/20/17.
 */
public class JSONUtils {

    private static ObjectMapper mapper;

    static {
        mapper = new ObjectMapper();
        // non-ascii characters will be escaped in json string
        mapper.configure(JsonGenerator.Feature.ESCAPE_NON_ASCII, true);
        // null value will not be included in json string
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        // unknown properties will be ignored during deserialization
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    /**
     * Return json format string of a given POJO
     *
     * @param obj object to be interpreted into json format
     * @return json format string of given object
     * @throws JsonProcessingException if obj cannot be converted to json string
     */
    public static String toJson(Object obj) throws JsonProcessingException {
        return mapper.writeValueAsString(obj);
    }

    /**
     * Return POJO reflected from JsonNode object
     *
     * @param node JsonNode object of class c
     * @param c    Class type of returned object
     * @param <T>  type of returned object
     * @return instance of class c corresponding to JsonNode
     * @throws JsonProcessingException if failed to convert JsonNode to POJO
     */
    public static <T> T fromJsonNode(JsonNode node, Class<T> c) throws JsonProcessingException {
        return mapper.treeToValue(node, c);
    }

    /**
     * Return JsonNode object of a given POJO
     *
     * @param obj object to be interpreted into JsonNode
     * @return JsonNode of given object
     */
    public static JsonNode toJsonNode(Object obj) {
        return mapper.valueToTree(obj);
    }

    public static Map<String, Object> toMap(Object obj) {
        return mapper.convertValue(obj, Map.class);
    }

    public static <T> T fromMap(Map<String, Object> m, Class<T> c) {
        return mapper.convertValue(m, c);
    }

    /**
     * Try to find the json node under the specific path of the root node
     *
     * @param path path of attributes from root to the result, separated by comma
     * @param root root of the path
     * @return an Optional object which may contain a json node with the specific path if present (including Null node),
     * otherwise an empty Optional instance
     */
    public static Optional<JsonNode> getNodeByPath(String path, JsonNode root) {
        JsonNode current = root;
        String[] list = path.split("\\.");
        for (String name : list) {
            if (current.isObject() && current.has(name))
                current = current.get(name);
            else return Optional.empty();
        }
        return Optional.of(current);
    }

    /**
     * Merge two JsonNode together recursively, the leaf nodes that both exist in two nodes will be override
     *
     * @param original original JsonNode to be updated, should not be null
     * @param override JsonNode that will override the leaf node with the same path in original, should not be null
     * @return merged JsonNode
     * @throws IOException if json conversion failed
     */
    public static JsonNode mergeJsonNodes(JsonNode original, JsonNode override) throws IOException {
        ObjectNode merged;
        if (original instanceof ObjectNode) {
            // no need to create another ObjectNode if original is actually an ObjectNode
            merged = (ObjectNode) original;
        } else {
            merged = (ObjectNode) mapper.readTree(original.asText());
        }
        Iterator<Map.Entry<String, JsonNode>> it = override.fields();
        while (it.hasNext()) {
            Map.Entry<String, JsonNode> entry = it.next();
            String key = entry.getKey();
            JsonNode value = entry.getValue();
            if (merged.has(key)) {
                if (merged.get(key).getNodeType() == value.getNodeType()) {
                    if (value.isValueNode() || value.isArray()) {
                        // use value in override node directly
                        merged.set(key, value);
                    } else {
                        // recursively merge child nodes
                        merged.set(key, mergeJsonNodes(merged.get(key), value));
                    }
                } else {
                    // cannot merge two nodes of different type
                    throw new IllegalArgumentException("Cannot merge nodes of different type: "
                            + merged.get(key).getNodeType().name() + " and " + value.getNodeType().name());
                }
            } else {
                // use value in override node directly
                merged.set(key, value);
            }
        }
        return merged;
    }

    public static <T> T convertValue(Object obj, Class<T> toValueType) {
        return mapper.convertValue(obj, toValueType);
    }

}
