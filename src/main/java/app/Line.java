package app;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import misc.Misc;
import misc.Vector2d;

import java.util.Objects;

public class Line {
    /**
     * две точки прямой
     */
    public Vector2d pos1;
    public Vector2d pos2;


    /**
     * Параметры в уравнении прямой
     */
    double k;
    double b;


    /**
     * Конструктор прямой через векторы
     *
     * @param pos1
     * @param pos2
     * положение прямой
     */
    public Line(@JsonProperty("pos") Vector2d pos1, Vector2d pos2) {
        this.pos1 = pos1;
        this.pos2 = pos2;
    }

    /**
     * Конструктор прямой через угловой коэффициент
     * @param k
     * @param b
     */
    public Line(double k, double b) {
        this.k = k;
        this.b = b;
    }


    /**
     * Получить положение
     * (нужен для json)
     *
     * @return положение
     */
    public Line getLine() {
        double x1 = pos1.x;
        double y1 = pos1.y;
        double x2 = pos2.x;
        double y2 = pos2.y;
        double k = (y2 - y1) / (x2 - x1);
        double b = y2 - x2 * k;
        Line line = new Line(k, b);
        return line;
    }



    /**
     * Получить цвет прямой
     *
     * @return цвет прямой
     */
    @JsonIgnore
    public int getColor() {
        return Misc.getColor(0xCC, 0x00, 0x00, 0xFF);
    }



    /**
     * Строковое представление объекта
     *
     * @return строковое представление объекта
     */
    @Override
    public String toString() {
        return "Line{" +
                "pos1=" + pos1 + ' ' + ", pos2=" + pos2 +
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
        Line line = (Line) o;
        return Objects.equals(line, o);
    }


    /**
     * Получить хэш-код объекта
     *
     * @return хэш-код объекта
     */
    @Override
    public int hashCode() {
        return Objects.hash(pos1, pos2);
    }
}
