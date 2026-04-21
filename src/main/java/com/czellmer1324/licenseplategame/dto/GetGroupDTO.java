package com.czellmer1324.licenseplategame.dto;

import java.time.ZonedDateTime;
import java.util.ArrayList;
public record GetGroupDTO(String groupName, String groupOwnerUserName, ArrayList<SafeUserDTO> members, ZonedDateTime endDate, long groupId) {
}
