package common.base;

import common.model.MessageResponse;

import java.util.List;

public interface BaseRestService<D extends BaseDTO> {

    //GET
    D get(Long id);
    List<D> getAll();

    //POST
    MessageResponse save(D dto);
    MessageResponse saveAll(List<D> dtos);

    //DELETE
    MessageResponse delete(Long id);
    MessageResponse deleteAll(List<Long> ids);

    //PUT - PATCH
    MessageResponse update(D dto, Long id);

}
