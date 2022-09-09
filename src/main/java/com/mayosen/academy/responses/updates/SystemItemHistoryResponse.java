package com.mayosen.academy.responses.updates;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class SystemItemHistoryResponse {
    private List<SystemItemHistoryUnit> items;
}
