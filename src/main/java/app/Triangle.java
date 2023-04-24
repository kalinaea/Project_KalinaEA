package app;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.humbleui.skija.Canvas;
import io.github.humbleui.skija.Paint;
import lombok.Getter;
import misc.CoordinateSystem2d;
import misc.CoordinateSystem2i;
import misc.Vector2d;
import misc.Vector2i;

import java.util.ArrayList;

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
}
