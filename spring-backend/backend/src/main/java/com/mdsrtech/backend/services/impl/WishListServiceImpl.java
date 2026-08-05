package com.mdsrtech.backend.services.impl;

import com.mdsrtech.backend.domain.entities.WishList;
import com.mdsrtech.backend.repositories.WishListRepository;
import com.mdsrtech.backend.services.WishListService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WishListServiceImpl implements WishListService {

    private final WishListRepository wishListRepository;
}