package com.g13cs3219.server.mapper;

/**
 * A generic interface for mapping between two types, A and B. This can be used to convert between DTOs and entities,
 * for example: userEntity to user.
 * @param <A>
 * @param <B>
 */
public interface Mapper<A, B> {

    B toDto(A a);

    A toEntity(B b);
}
