package common.base;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.core.ResolvableType;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Slf4j
public abstract class BaseGenericConverter<E extends BaseEntity, D extends BaseDTO>
        implements BaseConverter<E, D> {

    private final Class<D> classD;
    private final Class<E> classE;

    //Serve per istanziare una qualsiasi classe generica
    @SuppressWarnings("unchecked")
    public BaseGenericConverter() {
        ResolvableType clazz = ResolvableType.forClass(this.getClass()).getSuperType();
        while (!Objects.equals(clazz.resolve(), BaseGenericConverter.class)) {
            clazz = clazz.getSuperType();
        }
        final List<?> genericsClasses = Arrays.stream(clazz.getGenerics()).map(ResolvableType::resolve)
                .toList();

        this.classE = (Class<E>) genericsClasses.stream()
                .filter(o -> BaseEntity.class.isAssignableFrom((Class<?>) o))
                .findAny().get();

        this.classD = (Class<D>) genericsClasses.stream()
                .filter(o -> BaseDTO.class.isAssignableFrom((Class<?>) o))
                .findAny().get();
    }

    @Override
    public D toDTO(final E entity) {
        final D dto = BeanUtils.instantiateClass(this.classD);
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }

    @Override
    public E toEntity(final D dto) {
        final E entity = BeanUtils.instantiateClass(this.classE);
        BeanUtils.copyProperties(dto, entity);
        return entity;
    }

    @Override
    public E updateEntity(final D source, final E target) {
        BeanUtils.copyProperties(source, target);
        return target;
    }

}