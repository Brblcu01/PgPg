package common.base;

public class BaseController <S extends BaseRestService<D>, D extends BaseDTO> {

    protected final S service;

    protected BaseController(S service) {
        this.service = service;
    }

}
