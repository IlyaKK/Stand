package org.mai.data;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

//аннотацией Data объединилиси Геттер, Сеттер и конструктор без параметров
//Аннотацией всем значениям поставили уровень доступа private
// Добавляем автоматическую генерацию геттеров/сеттеров и пустого конструктора
@Data
// Делаем все поля приватными
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DataControlImitator {
    double kp;
    double ki;
    double kd;
    double jN;
    double kv;
}