package org.zenframework.easyservices.serialize.json;

import java.util.List;
import java.util.Map;

import org.zenframework.easyservices.descriptor.ValueDescriptor;
import org.zenframework.easyservices.serialize.Serializer;
import org.zenframework.easyservices.serialize.json.adapters.ListJsonSerializerAdapter;
import org.zenframework.easyservices.serialize.json.adapters.MapJsonSerializerAdapter;

import com.google.gson.Gson;

import junit.framework.TestCase;

public class JsonSerializerTest extends TestCase {

    private static final String TEST_JSON_ARR = "[{'str':'qwe','i':1}, {'str':'asd','i':2}]";
    private static final String TEST_JSON_OBJ = "{ a : {'str':'qwe','i':1}, b : {'str':'asd','i':2} }";

    public void testStrToArgs() throws Exception {
        Serializer serializer = new JsonSerializer(null, new Gson());
        Object[] result = serializer.deserialize(TEST_JSON_ARR, new Class<?>[] { SimpleBean.class, SimpleBean.class }, new ValueDescriptor[2]);
        assertTrue(result.length == 2);
        for (Object o : result)
            assertTrue(o instanceof SimpleBean);
    }

    public void testNullSerialization() throws Exception {
        Serializer serializer = new JsonSerializer(null, new Gson());
        assertEquals("null", serializer.serialize((Object) null));
        assertNull(serializer.deserialize("null", SimpleBean.class));
    }

    @SuppressWarnings("unchecked")
    public void testListSerialization() throws Exception {
        Serializer serializer = new JsonSerializer(null, new Gson());
        ValueDescriptor descriptor = new ValueDescriptor();
        descriptor.addAdapter(new ListJsonSerializerAdapter());
        descriptor.setTypeParameters(SimpleBean.class);
        List<SimpleBean> list = (List<SimpleBean>) serializer.deserialize(TEST_JSON_ARR, List.class, descriptor);
        assertEquals(new SimpleBean("qwe", 1), list.get(0));
        assertEquals(new SimpleBean("asd", 2), list.get(1));
    }

    @SuppressWarnings("unchecked")
    public void testMapSerialization() throws Exception {
        Serializer serializer = new JsonSerializer(null, new Gson());
        ValueDescriptor descriptor = new ValueDescriptor();
        descriptor.addAdapter(new MapJsonSerializerAdapter());
        descriptor.setTypeParameters(String.class, SimpleBean.class);
        Map<String, SimpleBean> map = (Map<String, SimpleBean>) serializer.deserialize(TEST_JSON_OBJ, Map.class, descriptor);
        assertEquals(new SimpleBean("qwe", 1), map.get("a"));
        assertEquals(new SimpleBean("asd", 2), map.get("b"));
    }

    public static class SimpleBean {

        private String str;
        private int i;

        public SimpleBean() {}

        public SimpleBean(String str, int i) {
            this.str = str;
            this.i = i;
        }

        public String getStr() {
            return str;
        }

        public void setStr(String str) {
            this.str = str;
        }

        public int getI() {
            return i;
        }

        public void setI(int i) {
            this.i = i;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof SimpleBean))
                return false;
            SimpleBean bean = (SimpleBean) obj;
            return i == bean.getI() && (str == bean.getStr() || str.equals(bean.str));
        }

    }

}
