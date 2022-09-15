package com.mayosen.academy.utils;

/**
 * Утилита для работы с URL.
 */
public class PathUtil {
    /**
     * Меняет пустой указатель на пустую строку, либо возвращает строку без изменений.
     * Нужно для обработки элементов с id, равным пустой строке.
     * @param id строки, которую нужно обработать
     * @return итоговая строка
     * @see com.mayosen.academy.controllers.MainController
     */
    public static String processNullId(String id) {
        return id == null ? "" : id;
    }
}
