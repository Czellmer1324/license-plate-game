package com.czellmer1324.licenseplategame.controller;

import com.czellmer1324.licenseplategame.dto.ChangeEndDateDTO;
import com.czellmer1324.licenseplategame.dto.CreateGroupDTO;
import com.czellmer1324.licenseplategame.dto.InviteDTO;
import com.czellmer1324.licenseplategame.dto.ServiceResponse;
import com.czellmer1324.licenseplategame.services.GroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.ZonedDateTime;

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

    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteGroup() {
        ServiceResponse responseInfo = service.deleteGroup();
        return ResponseEntity.status(responseInfo.code()).body(responseInfo.response());
    }

    @PutMapping("/invite/{groupId}")
    public ResponseEntity<?> inviteToGroup(@PathVariable long groupId, @RequestBody InviteDTO inviteDTO) {
        ServiceResponse responseInfo = service.inviteUserToGroup(groupId, inviteDTO);
        return ResponseEntity.status(responseInfo.code()).body(responseInfo.response());
    }

    @GetMapping("/get-groups")
    public ResponseEntity<?> getUserGroups() {
        ServiceResponse responseInfo = service.getUserGroups();
        return ResponseEntity.status(responseInfo.code()).body(responseInfo.response());
    }

    @GetMapping("/info/{groupId}")
    public ResponseEntity<?> getGroupInfo(@PathVariable long groupId) {
        ServiceResponse responseInfo = service.getGroupInfo(groupId);
        return ResponseEntity.status(responseInfo.code()).body(responseInfo.response());
    }

    @PutMapping("/update-end")
    public ResponseEntity<?> changeEndDate(@RequestBody ChangeEndDateDTO info) {
        ServiceResponse responseInfo = service.changeEndDate(info.newDate());
        return ResponseEntity.status(responseInfo.code()).body(responseInfo.response());
    }

    @PutMapping("/remove-user/{userName}")
    public ResponseEntity<?> removeUser(@PathVariable String userName) {
        ServiceResponse responseInfo = service.removeUser(userName);
        return ResponseEntity.status(responseInfo.code()).body(responseInfo.response());
    }

    @GetMapping("/member-map/{userId}/{groupId}")
    public ResponseEntity<?> getMemberFoundStates(@PathVariable int userId, @PathVariable long groupId) {
        ServiceResponse responseInfo = service.getMemberFoundStates(userId, groupId);
        return ResponseEntity.status(responseInfo.code()).body(responseInfo.response());
    }
}
