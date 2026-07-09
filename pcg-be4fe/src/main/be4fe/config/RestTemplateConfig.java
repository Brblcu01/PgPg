package be4fe.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Configurazione del client HTTP {@link RestTemplate} usato per chiamare il microservizio SSO.
 *
 * <p>Il {@code RestTemplate} è il componente centrale con cui BE4FE comunica con SSO per:</p>
 * <ul>
 *   <li>Avviare il flusso di autorizzazione Azure AD</li>
 *   <li>Scambiare l'authorization code con un JWT (callback)</li>
 *   <li>Recuperare le informazioni dell'utente autenticato</li>
 * </ul>
 *
 * <p>In futuro si può sostituire con {@code WebClient} (reactive) o aggiungere
 * interceptor per logging, timeout e retry.</p>
 */
@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

}
