package app;

import misc.Vector2d;

public class Triangle {
    /**
     * Координаты вершин треугольника
     */
    public Vector2d pos1;
    public Vector2d pos2;
    public Vector2d pos3;
    Triangle(Vector2d pos1, Vector2d pos2, Vector2d pos3) {
        if (pos1.equals(pos2) == false && pos1.equals(pos3) == false && pos2.equals(pos3) == false) {

        }
    }
}
