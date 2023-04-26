package app;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.humbleui.skija.Canvas;
import io.github.humbleui.skija.Paint;
import lombok.Getter;
import misc.*;

import java.util.ArrayList;
import java.util.Objects;

public class Triangle {
    /**
     * Координаты вершин треугольника
     */
    @Getter
    public Vector2d pos1;
    @Getter
    public Vector2d pos2;
    @Getter
    public Vector2d pos3;

    Triangle(@JsonProperty("pos1") Vector2d pos1,
             @JsonProperty("pos2") Vector2d pos2,
             @JsonProperty("pos3") Vector2d pos3) {
        this.pos1 = pos1;
        this.pos2 = pos2;
        this.pos3 = pos3;
    }

    @Override
    public String toString() {
        return "Straight{" +
                ", pos1=" + pos1 + ", pos2=" + pos2 + ", pos3=" + pos3 +
                '}';
    }


    @Override
    public int hashCode() {
        return Objects.hash(pos1, pos2, pos3);
    }


    /**
     * Получить цвет треугольника
     *
     * @return цвет треугольника
     */
    @JsonIgnore
    public int getColor() {
        return Misc.getColor(0xCC, 0x00, 0x00, 0xFF);
    }


    /**
     * Рисование треугольника
     * @param canvas
     * @param windowCS
     * @param ownCS
     */
    public void render(Canvas canvas, CoordinateSystem2i windowCS, CoordinateSystem2d ownCS) {
        try (Paint p = new Paint()) {
            // вершины треугольника
            Vector2i pointA = windowCS.getCoords(pos1, ownCS);
            Vector2i pointB = windowCS.getCoords(pos2, ownCS);
            Vector2i pointC = windowCS.getCoords(pos3, ownCS);
            // рисуем его стороны
            canvas.drawLine(pointA.x, pointA.y, pointB.x, pointB.y, p);
            canvas.drawLine(pointB.x, pointB.y, pointC.x, pointC.y, p);
            canvas.drawLine(pointC.x, pointC.y, pointA.x, pointA.y, p);
        }
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
        Triangle triangle = (Triangle) o;
        return Objects.equals(triangle, o);
    }

    /**
     * геттер треугольника
     * @return
     */
    public Triangle getTriangle() {
        Triangle triangle = new Triangle(pos1, pos2, pos3);
        return triangle;
    }


}
