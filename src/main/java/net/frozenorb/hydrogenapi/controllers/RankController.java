package net.frozenorb.hydrogenapi.controllers;

import net.frozenorb.hydrogenapi.repository.RankRepository;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
@RestController
@RequestMapping(path = "/ranks")
public class RankController {

    @Autowired private RankRepository rankRepository;

    @GetMapping
    public ResponseEntity<List> getRanks(){
        List<JSONObject> ranks = new ArrayList<>();

        rankRepository.findAll().forEach(rank -> ranks.add(rank.toJSON()));

        return new ResponseEntity<>(ranks, HttpStatus.OK);
    }

}
