package org.flowable.idm.engine.test.api.identity;

import org.flowable.idm.api.PasswordEncoder;
import org.flowable.idm.api.PasswordSalt;
import org.flowable.idm.api.User;
import org.flowable.idm.engine.impl.authentication.ApacheDigester;
import org.flowable.idm.engine.impl.authentication.ApacheDigester.Digester;
import org.flowable.idm.engine.impl.authentication.PasswordSaltImpl;
import org.flowable.idm.engine.test.PluggableFlowableIdmTestCase;
import org.flowable.idm.engine.test.api.identity.authentication.JasyptPasswordEncryptor;
import org.flowable.idm.engine.test.api.identity.authentication.jBCryptHashing;
import org.jasypt.util.password.StrongPasswordEncryptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by faizal on 6/10/17.
 */
public class PasswordEncoderTest extends PluggableFlowableIdmTestCase {

    private static Logger log = LoggerFactory.getLogger(PasswordEncoderTest.class);

    private void validatePassword() {
        User user = idmIdentityService.newUser("johndoe");
        user.setPassword("xxx");
        idmIdentityService.saveUser(user);

        User johndoe = idmIdentityService.createUserQuery().userId("johndoe").list().get(0);
        log.info("Hash Password = {} ", johndoe.getPassword());

        assertFalse("xxx".equals(johndoe.getPassword()));
        assertTrue(idmIdentityService.checkPassword("johndoe", "xxx"));
        assertFalse(idmIdentityService.checkPassword("johndoe", "invalid pwd"));

        idmIdentityService.deleteUser("johndoe");

    }

    public void testApacheDigesterdEncoderInstance() {
        PasswordEncoder passwordEncoder = idmEngineConfiguration.getPasswordEncoder();

        idmEngineConfiguration.setPasswordEncoder(new ApacheDigester(ApacheDigester.Digester.MD5));
        validatePassword();

        idmEngineConfiguration.setPasswordEncoder(new ApacheDigester(ApacheDigester.Digester.SHA512));
        validatePassword();
        
        idmEngineConfiguration.setPasswordEncoder(passwordEncoder);
    }

    public void testJasptEncoderInstance() {
        PasswordEncoder passwordEncoder = idmEngineConfiguration.getPasswordEncoder();
        idmEngineConfiguration.setPasswordEncoder(new JasyptPasswordEncryptor(new StrongPasswordEncryptor()));
        validatePassword();

        idmEngineConfiguration.setPasswordEncoder(passwordEncoder);
    }

    public void testjBCrytpEncoderInstance() {
        PasswordEncoder passwordEncoder = idmEngineConfiguration.getPasswordEncoder();
        idmEngineConfiguration.setPasswordEncoder(new jBCryptHashing());
        validatePassword();

        idmEngineConfiguration.setPasswordEncoder(passwordEncoder);
    }

    public void testSaltPasswordEncoderInstance() {
        PasswordEncoder passwordEncoder = idmEngineConfiguration.getPasswordEncoder();
        idmEngineConfiguration.setPasswordEncoder(new ApacheDigester(Digester.MD5));

        User user = idmIdentityService.newUser("johndoe");
        user.setPassword("xxx");
        idmIdentityService.saveUser(user);

        String noSalt = idmIdentityService.createUserQuery().userId("johndoe").list().get(0).getPassword();
        assertTrue(idmIdentityService.checkPassword("johndoe", "xxx"));
        idmIdentityService.deleteUser("johndoe");

        idmEngineConfiguration.setPasswordSalt(new PasswordSaltImpl("salt"));
        user = idmIdentityService.newUser("johndoe1");
        user.setPassword("xxx");
        idmIdentityService.saveUser(user);

        String salt = idmIdentityService.createUserQuery().userId("johndoe1").list().get(0).getPassword();
        assertTrue(idmIdentityService.checkPassword("johndoe1", "xxx"));

        assertFalse(noSalt.equals(salt));
        idmIdentityService.deleteUser("johndoe1");
        
        idmEngineConfiguration.setPasswordEncoder(passwordEncoder);
    }



    public void testValidatePasswordEncoderInstance() {
        PasswordEncoder passwordEncoder = idmEngineConfiguration.getPasswordEncoder();
        idmEngineConfiguration.setPasswordEncoder(new CustomPasswordEncoder());
        PasswordEncoder customPasswordEncoder = idmEngineConfiguration.getPasswordEncoder();
        assertTrue(customPasswordEncoder instanceof CustomPasswordEncoder);
        
        idmEngineConfiguration.setPasswordEncoder(passwordEncoder);
    }


    class CustomPasswordEncoder implements PasswordEncoder {

        public String encode(CharSequence rawPassword, PasswordSalt passwordSalt) {
            return null;
        }

        public boolean isMatches(CharSequence rawPassword, String encodedPassword, PasswordSalt salt) {
            return false;
        }
    }


}
