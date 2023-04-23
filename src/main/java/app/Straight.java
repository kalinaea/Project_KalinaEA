package app;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import misc.Misc;
import misc.Vector2d;

import java.util.Objects;

public class Straight {
    /**
     * две точки прямой
     */
    public Vector2d pos1;
    public Vector2d pos2;


    /**
     * Конструктор прямой
     *
     * @param pos1 положение прямой
     */
    public Straight(@JsonProperty("pos") Vector2d pos1, Vector2d pos2) {
        this.pos1 = pos1;
        this.pos2 = pos2;
    }


    /**
     * Получить положение
     * (нужен для json)
     *
     * @return положение
     */
    public Vector2d getStraight() {
        Vector2d straight = pos1.subtract(pos2);
        return straight;
    }

    /**
     * Строковое представление объекта
     *
     * @return строковое представление объекта
     */
    @Override
    public String toString() {
        return "Straight{" +
                ", pos1=" + pos1 + ' ' + "pos2=" + pos2 +
                '}';
    }

    /**
     * Проверка двух объектов на равенство
     *
     * @param o объект, с которым сравниваем текущий
     * @return флаг, равны ли два объекта
     */
    @Override
    public boolean equals(Object o) {
        // если объект сравнивается сам с собой, тогда объекты равны
        if (this == o) return true;
        // если в аргументе передан null или классы не совпадают, тогда объекты не равны
        if (o == null || getClass() != o.getClass()) return false;
        // приводим переданный в параметрах объект к текущему классу
        Straight straight = (Straight) o;
        return Objects.equals(straight, o);
    }


    /**
     * Получить хэш-код объекта
     *
     * @return хэш-код объекта
     */
    @Override
    public int hashCode() {
        return Objects.hash(pos1.subtract(pos2));
    }
}
