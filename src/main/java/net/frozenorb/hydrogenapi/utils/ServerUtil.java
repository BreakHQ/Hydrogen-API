package net.frozenorb.hydrogenapi.utils;

import net.frozenorb.hydrogenapi.HydrogenAPI;
import org.json.simple.JSONObject;
import redis.clients.jedis.Jedis;

import java.util.*;
public class ServerUtil {

    private static Jedis jedis = HydrogenAPI.getRedisManager().getJedis();

    public static void update(String name, Map<String, Object> body){
        String key = "servers:" + name;
        jedis.hset(key, "id", name);
        jedis.hset(key, "displayName", name);
        jedis.hset(key, "serverGroup", name);
        jedis.hset(key, "serverIp", "127.0.0.1");
        jedis.hset(key, "lastTps", body.get("lastTps").toString());
        jedis.hset(key, "lastUpdatedAt", System.currentTimeMillis() / 1000 + "");
        jedis.expire(key, 40); // heartbeat is send every 30sec so let's give it 10 more as a buffer

        jedis.sadd("servers", name);
    }

    public static Set<JSONObject> getServersAsJSON(){
        Set<JSONObject> servers = new HashSet<>();

        jedis.keys("servers:*").forEach(key -> {
            JSONObject json = new JSONObject();
            json.putAll(jedis.hgetAll(key));
            servers.add(json);
        });

        return servers;
    }

}
