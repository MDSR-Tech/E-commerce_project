package com.mdsrtech.backend.controllers;

import com.mdsrtech.backend.services.WishListService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api/wishlist")
@RequiredArgsConstructor
public class WishListController {

    private final WishListService wishListService;



}