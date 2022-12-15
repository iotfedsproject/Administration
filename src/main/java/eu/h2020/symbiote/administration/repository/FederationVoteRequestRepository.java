package eu.h2020.symbiote.administration.repository;

import eu.h2020.symbiote.administration.model.FederationVoteRequest.FederationVoteRequest;
import eu.h2020.symbiote.administration.model.enums.RequestStatus;
import eu.h2020.symbiote.administration.model.enums.VoteAction;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


/**
 * @author Konstantinos Vasilopoulos (ICOM)
 * @since 08/08/2022.
 */
//@RepositoryRestResource(collectionResourceRel = "federations", path = "federations")
@Repository
public interface FederationVoteRequestRepository extends MongoRepository<FederationVoteRequest, String> {

//    Optional<FederationWithInvitations> deleteById(String id);

    @Query("{'username' : ?0,  'status' : ?1,   'voteAction' : ?2, 'federationId' : ?3}")
    List<FederationVoteRequest> findAllForUserByStatusFederationAndVoteAction(String username, RequestStatus status, VoteAction voteAction,String federationId);

    @Query("{'username' : ?0}")
    List<FederationVoteRequest> findAllForUser(String username);

    @Query("{'username' : ?0,  'status' : ?1,   'voteAction' : ?2}")
    List<FederationVoteRequest> findAllForUserByStatusAndVoteAction(String username, RequestStatus status, VoteAction voteAction);

    @Query("{'federationId' : ?0}")
    List<FederationVoteRequest> findAllByFederationId(String federationId);

//    List<FederationJoinRequest> findAll();
    List<FederationVoteRequest> findByStatus(RequestStatus status);

//    @Query("{'status':PENDING}")
//    List<FederationJoinRequest> findAllPending();

    Optional<FederationVoteRequest> findByVotingId(String votingId);
}
