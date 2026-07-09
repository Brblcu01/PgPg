package common.utils;

import common.model.MessageResponse;
import lombok.experimental.UtilityClass;

import java.time.LocalDateTime;

@UtilityClass
public final class MessageResponseFactory {

    public static MessageResponse created() {
        return new MessageResponse(
                201,
                "Inserimento avvenuto con successo",
                LocalDateTime.now()
        );
    }

    public static MessageResponse ok() {
        return new MessageResponse(
                200,
                "Modifica avvenuto con successo",
                LocalDateTime.now()
        );
    }

    public static MessageResponse deleted() {
        return new MessageResponse(
                204,
                "Rimozione avvenuto con successo",
                LocalDateTime.now()
        );
    }
}
