import io.github.gapplex.gxu.container.CapacityUtils;
import io.github.gapplex.gxu.container.MapModifier;
import io.github.gapplex.gxu.container.Maps;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;

public class Test {
    public static void main(String[] args) {
        Map<String, Object> map = new HashMap<>();
        map.put("hi", "World");

        MapModifier<String, Object> mapModifier = new MapModifier<>(map)
                .put("2", 2);

        System.out.println(Maps.unmodifiableMap(null));

        AtomicInteger i = new AtomicInteger(0);
        Maps.MapBuilder mapBuilder = Maps.mapOf((size) -> {
            i.incrementAndGet();
            return new HashMap<>(CapacityUtils.capacity(size));
        });
        i.set(0);
    }
}
