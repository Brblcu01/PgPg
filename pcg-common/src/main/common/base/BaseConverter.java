package common.base;

import java.util.Collection;
import java.util.stream.Stream;

/**
 * Interface implemented by the application converters. Contains the method signatures
 * necessary to generate entity objects from DTOs and vice versa.
 *
 * @param <E> parameter type
 * @param <D> generic type
 */
public interface BaseConverter<E extends BaseEntity, D extends BaseDTO> {

    /**
     * Creates a DTO from an entity.
     *
     * @param entity The entity
     * @return DTO
     */
    D toDTO(E entity);

    /**
     * Creates an entity from a DTO.
     *
     * @param dto The DTO
     * @return entity
     */
    E toEntity(D dto);

    /**
     * Creates a list of entities from a list of DTOs.
     *
     * @param dtos List of DTOs
     * @return stream of entities
     */
    default Stream<E> toEntities(final Collection<D> dtos) {
        return dtos.stream().map(this::toEntity);
    }

    /**
     * Creates a list of DTOs from a list of entities.
     *
     * @param entities List of entities
     * @return stream of DTOs
     */
    default Stream<D> toDTOS(final Collection<E> entities) {
        return entities.stream().map(this::toDTO);
    }

    /**
     * Updates an entity.
     *
     * @param source      the source value
     * @param target      the target value
     * @return updated entity
     */
    E updateEntity(D source, E target);

}