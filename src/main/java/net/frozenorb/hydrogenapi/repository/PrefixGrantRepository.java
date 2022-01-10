package net.frozenorb.hydrogenapi.repository;

import net.frozenorb.hydrogenapi.models.PrefixGrant;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface PrefixGrantRepository extends MongoRepository<PrefixGrant, String> {

    PrefixGrant findById(String id);
    List<PrefixGrant> findByUuid(String uuid);

}
