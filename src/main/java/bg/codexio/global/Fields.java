package bg.codexio.global;

import bg.codexio.CustomURLClassLoader;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class Fields {
    public static final Map<String, Object> typeMap = new HashMap<>();

    public static final CustomURLClassLoader customURLClassLoader = new CustomURLClassLoader(new URL[0]);
}
