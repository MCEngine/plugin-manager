package io.github.mcengine.pluginmanager.common.manager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class JsonTest {

    @Test
    @DisplayName("reads the shape the desired-state route returns")
    void readsDesiredState() {
        String payload = """
            {
              "actions": [
                {
                  "action": "update",
                  "plugin_id": "Essentials",
                  "product_id": "01PROD",
                  "from_version": "2.19.0",
                  "to_version": "2.20.1",
                  "download_url": "/api/v1/products/01PROD/versions/2.20.1/download",
                  "sha256": "9f86d081884c7d659a2feaa0c55ad015a3bf4f1b2b0b822cd15d6c15b0f00a08",
                  "size_bytes": 4194304
                },
                { "action": "delete", "plugin_id": "OldPlugin" }
              ],
              "poll_after_seconds": 300
            }
            """;

        Object parsed = Json.parse(payload);
        List<Object> actions = Json.array(parsed, "actions");

        assertEquals(2, actions.size());
        assertEquals("Essentials", Json.string(actions.get(0), "plugin_id"));
        assertEquals(4194304, Json.number(actions.get(0), -1, "size_bytes"));
        assertEquals(300, Json.number(parsed, -1, "poll_after_seconds"));
        assertEquals("delete", Json.string(actions.get(1), "action"));
    }

    @Test
    @DisplayName("returns null rather than throwing for a path that is not there")
    void missingPaths() {
        Object parsed = Json.parse("{\"a\":{\"b\":1}}");
        assertNull(Json.string(parsed, "a", "missing"));
        assertNull(Json.string(parsed, "nope", "b"));
        assertEquals(7, Json.number(parsed, 7, "a", "missing"));
        assertTrue(Json.array(parsed, "a", "missing").isEmpty());
    }

    @Test
    @DisplayName("reads escapes, including unicode")
    void escapes() {
        assertEquals("a\"b\\c\nd\te\u00e9",
            Json.string(Json.parse("{\"k\":\"a\\\"b\\\\c\\nd\\te\\u00e9\"}"), "k"));
    }

    @Test
    @DisplayName("reads nested arrays and objects")
    void nesting() {
        Object parsed = Json.parse("{\"a\":[{\"b\":[1,2,{\"c\":\"d\"}]}]}");
        List<Object> outer = Json.array(parsed, "a");
        List<Object> inner = Json.array(outer.get(0), "b");
        assertEquals("d", Json.string(inner.get(2), "c"));
    }

    @Test
    @DisplayName("reads an empty object and an empty array")
    void empties() {
        assertTrue(Json.array(Json.parse("{\"a\":[]}"), "a").isEmpty());
        assertNull(Json.string(Json.parse("{}"), "a"));
    }

    @Test
    @DisplayName("refuses malformed input rather than guessing")
    void refusesMalformed() {
        for (String bad : new String[] {
            "{", "}", "{\"a\":}", "[1,]x", "{\"a\" 1}", "\"unterminated", "{\"a\":\"\\q\"}",
        }) {
            assertThrows(Json.JsonException.class, () -> Json.parse(bad), bad);
        }
    }

    @Test
    @DisplayName("quotes what would otherwise break a document")
    void quoting() {
        assertEquals("\"a\\\"b\"", Json.quote("a\"b"));
        assertEquals("\"a\\\\b\"", Json.quote("a\\b"));
        assertEquals("\"a\\nb\"", Json.quote("a\nb"));
        assertTrue(Json.quote("a\u0001b").contains("\\u0001"));
    }

    @Test
    @DisplayName("survives a round trip through quote and parse")
    void roundTrip() {
        String awkward = "line\nbreak \"quoted\" back\\slash \u0007bell";
        assertEquals(awkward, Json.string(Json.parse("{\"k\":" + Json.quote(awkward) + "}"), "k"));
    }
}
