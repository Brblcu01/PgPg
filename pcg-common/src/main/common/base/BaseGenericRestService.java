package common.base;

import common.model.MessageResponse;
import common.utils.MessageResponseFactory;
import jakarta.persistence.EntityNotFoundException;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.util.List;

@NoArgsConstructor(force = true)
@RequiredArgsConstructor
public class BaseGenericRestService<E extends BaseEntity, D extends BaseDTO, R extends BaseRepository<E>>
        implements BaseRestService<D> {

    protected final R repository;
    private final BaseConverter<E, D> baseConverter;

    @Override
    public D get(Long id) {
        return this.baseConverter.toDTO(findById(id));
    }

    @Override
    public List<D> getAll() {
        return baseConverter.toDTOS(repository.findAll()).toList();
    }

    @Override
    public MessageResponse save(D dto) {
        E entity = this.baseConverter.toEntity(dto);
        entity.setId(null);
        this.repository.save(entity);
        return MessageResponseFactory.created();
    }

    @Override
    public MessageResponse saveAll(List<D> dtos) {
        List<E> entities = this.baseConverter.toEntities(dtos).toList();
        for(E entity : entities) { entity.setId(null); }
        this.repository.saveAll(entities);
        return MessageResponseFactory.created();
    }

    @Override
    public MessageResponse delete(Long id) {
        this.repository.deleteById(findById(id).getId());
        return MessageResponseFactory.deleted();
    }

    @Override
    public MessageResponse deleteAll(List<Long> ids) {
        for (Long id : ids) { findById(id); }
        this.repository.deleteAllById(ids);
        return MessageResponseFactory.deleted();
    }

    @Override
    public MessageResponse update(D dto, Long id) {
        this.repository.save(this.baseConverter.updateEntity(dto, findById(id)));
        return MessageResponseFactory.ok();
    }

    private E findById(Long id) {
        return this.repository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Entity not Found"));
    }

}