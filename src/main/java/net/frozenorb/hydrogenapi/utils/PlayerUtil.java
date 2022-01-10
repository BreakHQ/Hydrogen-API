package net.frozenorb.hydrogenapi.utils;

import net.frozenorb.hydrogenapi.HydrogenAPI;
import net.frozenorb.hydrogenapi.repository.PlayerRepository;
import net.frozenorb.hydrogenapi.repository.PrefixGrantRepository;
import net.frozenorb.hydrogenapi.repository.PunishmentRepository;
import net.frozenorb.hydrogenapi.repository.RankGrantRepository;
import net.frozenorb.hydrogenapi.models.Expirable;
import net.frozenorb.hydrogenapi.models.Player;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
@Service
public class PlayerUtil {

    @Autowired private PlayerRepository playerRepository;
    @Autowired private PunishmentRepository punishmentRepository;
    @Autowired private RankGrantRepository rankGrantRepository;
    @Autowired private PrefixGrantRepository prefixGrantRepository;

    public JSONObject getPlayerByUUID(String uuid, String username, String userIp){
        Player player = playerRepository.findByUuid(uuid);

        if(player == null){
            player = new Player();
            player.setUuid(uuid);
            player.setUsername(username);
            player.setLastSeenAt(System.currentTimeMillis() / 1000);

            playerRepository.save(player);
        }

        JSONObject json = player.toJSON();

        if (userIp != null) {
            punishmentRepository.findByUserIp(userIp)
                    .stream()
                    .filter(punishment -> punishment.getRemovedAt() != 0 && punishment.getType().equals("BLACKLIST"))
                    .forEach(punishment -> {
                        JSONObject access = new JSONObject();
                        access.put("allowed", false);
                        access.put("message", HydrogenAPI.getSettingsManager().getSettings().get("blacklist-message"));
                        json.put("access", access);
                    });
        }

        if (!json.containsKey("access")) {
            punishmentRepository.findByUuid(uuid)
                    .stream()
                    .filter(Expirable::isActive)
                    .forEach(punishment -> {
                JSONObject access = new JSONObject();

                switch (punishment.getType().toUpperCase()) {
                    case "BLACKLIST":
                        access.put("message", HydrogenAPI.getSettingsManager().getSettings().get("blacklist-message"));
                        break;
                    case "BAN":
                        access.put("message", HydrogenAPI.getSettingsManager().getSettings().get("blacklist-message"));
                        break;
                    case "MUTE":
                        json.put("mute", punishment.toJSON());
                        break;
                    default:
                        throw new RuntimeException("Unsupported punishment type: " + punishment.getType());
                }

                if (!access.isEmpty()) {
                    access.put("allowed", false);
                    json.put("access", access);
                }
            });
        }

        JSONObject scopeRanks = new JSONObject();
        List<String> ranks = new ArrayList<>();
        rankGrantRepository.findByUuid(uuid)
                .stream()
                .filter(Expirable::isActive)
                .forEach(rankGrant -> {
                    scopeRanks.put(rankGrant.getRank(), rankGrant.getScopes());
                    ranks.add(rankGrant.getRank());
                });
        json.put("scopeRanks", scopeRanks);

        // Add the default rank if they don't have a rank already
        if(ranks.size() == 0)
            ranks.add("default");

        json.put("ranks", ranks);

        List<String> prefixes = new ArrayList<>();
        prefixGrantRepository.findByUuid(uuid)
                .stream()
                .filter(Expirable::isActive)
                .forEach(prefixGrant -> {
                    prefixes.add(prefixGrant.getPrefix());
                });
        json.put("prefixes", prefixes);

        return json;
    }

    public void logIp(String uuid, String ip){
        Player player = playerRepository.findByUuid(uuid);

        if(!player.getIpLog().contains(ip))
            player.getIpLog().add(ip);

        playerRepository.save(player);
    }

    public void setOnline(String uuid, String servername){
        Player player = playerRepository.findByUuid(uuid);
        player.setOnline(true);
        player.setLastSeenOn(servername);
        playerRepository.save(player);
    }
}
