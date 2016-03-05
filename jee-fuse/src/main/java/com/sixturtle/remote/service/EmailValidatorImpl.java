package com.sixturtle.remote.service;

import javax.inject.Named;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sixturtle.exception.InvalidEntityException;
import com.sixturtle.exception.RemoteCallException;
import com.sixturtle.remote.AbstractRestClient;
import com.sixturtle.remote.ConfigLoader;

/**
 * Implements {@link EmailValidator} API.
 *
 * @author Anurag Sharma
 */
@Named
public class EmailValidatorImpl extends AbstractRestClient implements EmailValidator {
    private static final Logger log = LoggerFactory.getLogger(EmailValidator.class);

    private MashupContext context;

    /**
     * Default constructor to setup context
     */
    public EmailValidatorImpl() {
        super();
        loadContext();
    }

    /**
     * @return the context
     */
    public MashupContext getContext() {
        return context;
    }


    /* (non-Javadoc)
     * @see com.sixturtle.remote.EmailValidator#isValidEmail(java.lang.String)
     */
    @Override
    public boolean isValidEmail(String email) throws RemoteCallException, InvalidEntityException {
        boolean status = false;

        Response response = super.<Response>invoke(new InvokeCommand<Response>() {
            /*
             * (non-Javadoc)
             * @see com.sixturtle.remote.AbstractRestClient.InvokeCommand#execute()
             */
            @Override
            public Response execute() {
                Response response = getClient()
                                        .target(getContext().getBaseUrl() + getContext().getApiPath() + "?email=" + email)
                                        .request()
                                        .accept(MediaType.APPLICATION_JSON)
                                        .header(
                                            MashupContext.AUTH_HEADER,
                                            getContext().getAuthcode()
                                         )
                                        .get();
                log.debug("Email Validation Response: {}", response.getStatus());
                return response;
            }

        });
        EmailStatus s = response.readEntity(EmailStatus.class);
        if (s != null) {
            status = s.isValid();
        }
        return status;
    }

    /*
     * (non-Javadoc)
     * @see com.sixturtle.remote.AbstractRestClient#authenticate()
     */
    @Override
    protected void authenticate() throws RemoteCallException {
        // NONE: pre-authentication setup for this API call
    }

    /**
     * Loads {@link MashupContext} from a property file. The property file can be
     * hosted externally and passed as command line argument
     * -Dmashup.config=/path/to/mashupConfig.json or it can use the default
     * bundled property.
     */
    private void loadContext() {
        String configFilePath = System.getProperty("mashup.config");
        if (configFilePath == null) {
            configFilePath = "config/mashupConfig.json"; // default, bundled property
        }
        context = ConfigLoader.loadJsonFileContent(MashupContext.class, configFilePath);
        log.debug("loaded mashup context: {}", context);
    }
}
