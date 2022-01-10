package net.frozenorb.hydrogenapi.controllers;

import net.frozenorb.hydrogenapi.HydrogenAPI;
import net.frozenorb.hydrogenapi.repository.PlayerRepository;
import net.frozenorb.hydrogenapi.models.Player;
import net.frozenorb.hydrogenapi.utils.PlayerUtil;
import net.frozenorb.hydrogenapi.utils.ResponseUtil;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController @RequestMapping("/users/{uuid}")
public class UserController {

    @Autowired private PlayerRepository playerRepository;
    @Autowired private PlayerUtil playerUtil;

    @GetMapping
    public ResponseEntity<JSONObject> getPlayer(@PathVariable("uuid") String uuid){
        Player player = playerRepository.findByUuid(uuid);
        JSONObject response = new JSONObject();

        if(player == null){
            response.put("success", false);
            response.put("message", "Player hasn't joined the server before");
            return new ResponseEntity<>(response, HttpStatus.OK);
        }

        response.putAll(player.toJSON());
        response.put("lastUsername", player.getUsername());

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping(path = "/details")
    public ResponseEntity<JSONObject> getDetails(@PathVariable("uuid") String uuid){
        Player player = playerRepository.findByUuid(uuid);
        JSONObject response = new JSONObject();

        if(player == null){
            response.put("success", false);
            response.put("message", "Player hasn't joined the server before");
            return new ResponseEntity<>(response, HttpStatus.OK);
        }

        response.put("user", player.toJSON());
        response.put("ipLog", player.getIpLog());

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping(path = "/login")
    public ResponseEntity<JSONObject> login(@RequestHeader(value = "MHQ-Authorization") String apiKey, @PathVariable("uuid") String uuid, @RequestBody Map<String, String> body){
        String username = body.get("username");
        String userIp = body.get("userIp");

        // get the player data here so if ip logging is enabled there isn't an error
        JSONObject response = playerUtil.getPlayerByUUID(uuid, username, userIp);

        playerUtil.setOnline(uuid, apiKey);

        if(HydrogenAPI.getSettingsManager().getSettings().get("log-ips").equals("true"))
            playerUtil.logIp(uuid, userIp);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping(path = "/registerEmail")
    public ResponseEntity<JSONObject> registerEmail(@PathVariable("uuid") String uuid, @RequestBody Map<String, String> body){
        String email = body.get("email");
        String userIp = body.get("userIp");

        Player player = playerRepository.findByUuid(uuid);
        player.setEmail(email);
        playerRepository.save(player);

        return ResponseUtil.success;
    }

    @PostMapping(path = "/prefix")
    public ResponseEntity<JSONObject> updateActivePrefix(@PathVariable("uuid") String uuid, @RequestBody Map<String, String> body){
        String prefix = body.get("prefix");

        Player player = playerRepository.findByUuid(uuid);
        player.setActivePrefix(prefix);
        playerRepository.save(player);

        return ResponseUtil.success;
    }

    @PostMapping(path = "/colors")
    public ResponseEntity<JSONObject> updateColors(@PathVariable("uuid") String uuid, @RequestBody Map<String, String> body){
        String iconColor = body.get("iconColor");
        String nameColor = body.get("nameColor");

        Player player = playerRepository.findByUuid(uuid);
        player.setIconColor(iconColor);
        player.setNameColor(nameColor);
        playerRepository.save(player);

        return null;
    }

    @PostMapping(path = "/verifyTotp")
    public ResponseEntity<JSONObject> verifyTOTP(@PathVariable("uuid") String uuid, @RequestBody Map<String, String> body){
        String ip = body.get("userIp");
        String code = body.get("totpCode");

        JSONObject json = new JSONObject();
        json.put("authorized", true);
        json.put("message", "");
        return new ResponseEntity<>(json, HttpStatus.OK);
    }

    @GetMapping(path = "/requiresTotp")
    public ResponseEntity<JSONObject> requiresTotp(@PathVariable("uuid") String uuid){
        JSONObject json = new JSONObject();
        json.put("required", false);
        json.put("message", "NOT_REQUIRED_IP_PRE_AUTHORIZED");
        return new ResponseEntity<>(json, HttpStatus.OK);
    }

    @PostMapping(path = "/disposableLoginTokens")
    public ResponseEntity<JSONObject> getDisposableToken(@RequestBody Map<String, String> body){
        String uuid = body.get("user");
        String ip = body.get("userIp");

        JSONObject json = new JSONObject();

        Player player = playerRepository.findByUuid(uuid);
        if(player.getEmail() == null){
            json.put("success", false);
            json.put("message", "Your profile doesn't have an account.");
            return new ResponseEntity<>(json, HttpStatus.OK);
        }

        String token = UUID.randomUUID().toString();
        HydrogenAPI.getRedisManager().getJedis().hset("disposableTokens", token, uuid);

        json.put("token", token);
        return new ResponseEntity<>(json, HttpStatus.OK);
    }

}
