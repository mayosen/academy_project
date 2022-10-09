package com.mayosen.academy.responses.updates;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Объект для групповых запросов на элементы.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ItemHistoryResponse {
    private List<ItemHistoryUnit> items;
}
