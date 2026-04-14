package com.czellmer1324.licenseplategame.controller;

import com.czellmer1324.licenseplategame.dto.CreateGroupDTO;
import com.czellmer1324.licenseplategame.dto.ServiceResponse;
import com.czellmer1324.licenseplategame.services.GroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/group")
@RequiredArgsConstructor
public class GroupController {
    private final GroupService service;

    @PostMapping("/create")
    public ResponseEntity<?> createGroup(@RequestBody CreateGroupDTO groupInfo) {
        ServiceResponse responseInfo = service.createGroup(groupInfo);
        return ResponseEntity.status(responseInfo.code()).body(responseInfo.response());
    }
}
