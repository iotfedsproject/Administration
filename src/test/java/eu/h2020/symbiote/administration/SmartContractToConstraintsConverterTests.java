package eu.h2020.symbiote.administration;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.h2020.symbiote.administration.model.Baas.Federation.Rules.SmartContract;

import eu.h2020.symbiote.administration.model.CoreUser;
import eu.h2020.symbiote.model.mim.QoSConstraint;
import eu.h2020.symbiote.security.commons.enums.UserRole;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static eu.h2020.symbiote.administration.converters.SmartContractToConstraintsConverter.convertSmartContractToConstrains;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class SmartContractToConstraintsConverterTests extends AdministrationBaseTestClass{

    private SmartContract smartContract;

    @Before
    public void setup() throws IOException {

        ObjectMapper mapper = new ObjectMapper();

        smartContract = mapper.readValue(Paths.get("src/test/resources/newSmartContract.json").toFile(), SmartContract.class);
    }

    @After
    public void tearDown() {
    }

    @Test
    public void convertData(){
        List<QoSConstraint> qoSConstraintList = convertSmartContractToConstrains(smartContract);
        System.out.println(qoSConstraintList.get(0).getThreshold());
        assert  true;
    }

}
