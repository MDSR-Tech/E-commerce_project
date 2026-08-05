package com.mdsrtech.backend.mapper;

public interface Mapper<A, B> {

    B mapFromEntityToDTO(A a);
    A mapFromDTOToEntity(B b);

}
