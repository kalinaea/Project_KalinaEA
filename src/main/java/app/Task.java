package app;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.github.humbleui.jwm.MouseButton;
import io.github.humbleui.skija.Canvas;
import io.github.humbleui.skija.Paint;
import io.github.humbleui.skija.Rect;
import lombok.Getter;
import misc.CoordinateSystem2d;
import misc.CoordinateSystem2i;
import misc.Vector2d;
import misc.Vector2i;
import panels.PanelLog;

import java.util.ArrayList;

import static app.Colors.CROSSED_COLOR;
import static app.Colors.SUBTRACTED_COLOR;

/**
 * Класс задачи
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "@class")
public class Task {
    /**
     * Текст задачи
     */
    public static final String TASK_TEXT = """
            ПОСТАНОВКА ЗАДАЧИ:
            На плоскости задан треугольник и еще множество точек. 
            Необходимо найти такие две точки множества, что прямая, 
            проходящая через эти две точки, пересекает треугольник, 
            и при этом отрезок этой прямой, оказавшейся внутри
            треугольника, оказывается наибольшей длины. 
            В качестве ответа хотелось бы видеть
            выделенные эти две точки, прямую, через них 
            проходящую, и этот отрезок.""";

    /**
     * Размер точки
     */
    private static final int POINT_SIZE = 3;

    /**
     * Вещественная система координат задачи
     */
    @Getter
    private final CoordinateSystem2d ownCS;
    /**
     * Список точек
     */
    @Getter
    private final ArrayList<Point> points;


    /**
     * Список прямых
     */
    @Getter
    private final ArrayList<Line> lines;



    /**
     * Список треугольников
     */
    @Getter
    private final ArrayList<Triangle> triangles;

    /**
     * Задача
     *
     * @param ownCS  СК задачи
     * @param points массив точек
     */

    /**
     * последняя СК окна
     */
    protected CoordinateSystem2i lastWindowCS;

    /**
     * Флаг, решена ли задача
     */
    private boolean solved;


    /**
     * Список точек в пересечении
     */
    @Getter
    @JsonIgnore
    private final ArrayList<Point> crossed;
    /**
     * Список точек в разности
     */
    @Getter
    @JsonIgnore
    private final ArrayList<Point> single;



    @Getter
    Triangle triangle;


    public Task(
            @JsonProperty("ownCS") CoordinateSystem2d ownCS,
            @JsonProperty("points") ArrayList<Point> points,
            @JsonProperty("triangles") ArrayList<Triangle> triangles,
            @JsonProperty("lines") ArrayList<Line> lines

    ) {
        this.ownCS = ownCS;
        this.points = points;
        this.triangles = triangles;
        this.lines = lines;
        this.crossed = new ArrayList<>();
        this.single = new ArrayList<>();
    }



    /**
     * Очистить задачу
     */
    public void clear() {
        points.clear();
        solved = false;
        triangle = null;
    }

    /**
     * Рисование задачи
     *
     * @param canvas   область рисования
     * @param windowCS СК окна
     */

    public void paint(Canvas canvas, CoordinateSystem2i windowCS) {
        // Сохраняем последнюю СК
        lastWindowCS = windowCS;

        canvas.save();
        // создаём перо
        try (var paint = new Paint()) {
            if (triangle != null)
                triangle.render(canvas, windowCS, ownCS);
            for (Point p : points) {
                if (!solved) {
                    paint.setColor(p.getColor());
                } else {
                    if (crossed.contains(p))
                        paint.setColor(CROSSED_COLOR);
                    else
                        paint.setColor(SUBTRACTED_COLOR);
                }
                // y-координату разворачиваем, потому что у СК окна ось y направлена вниз,
                // а в классическом представлении - вверх
                Vector2i windowPos = windowCS.getCoords(p.pos.x, p.pos.y, ownCS);
                // рисуем точку
                canvas.drawRect(Rect.makeXYWH(windowPos.x - POINT_SIZE, windowPos.y - POINT_SIZE, POINT_SIZE * 2, POINT_SIZE * 2), paint);
            }
        }
        canvas.restore();
    }

    /**
     * Клик мыши по пространству задачи
     *
     * @param pos         положение мыши
     * @param mouseButton кнопка мыши
     */
    public void click(Vector2i pos, MouseButton mouseButton) {
        if (lastWindowCS == null) return;
        // получаем положение на экране
        Vector2d taskPos = ownCS.getCoords(pos, lastWindowCS);
        // если левая кнопка мыши, добавляем в первое множество
        if (mouseButton.equals(MouseButton.PRIMARY)) {
            addPoint(taskPos);
            // если правая, то во второе
        } else if (mouseButton.equals(MouseButton.SECONDARY)) {
            addPoint(taskPos);
        }
    }

    /**
     * Добавить точку
     *
     * @param pos положение
     */
    public void addPoint(Vector2d pos) {
        solved = false;
        Point newPoint = new Point(pos);
        points.add(newPoint);
        // Добавляем в лог запись информации
        PanelLog.info("точка " + newPoint + " добавлена");
    }


    /**
     * Решить задачу
     */
    public void solve() {
        // очищаем списки
        crossed.clear();
        single.clear();

        // перебираем пары точек
        for (int i = 0; i < points.size(); i++) {
            for (int j = i + 1; j < points.size(); j++) {
                // сохраняем точки
                Point a = points.get(i);
                Point b = points.get(j);
                // если точки совпадают по положению
                if (a.pos.equals(b.pos)) {
                    if (!crossed.contains(a)) {
                        crossed.add(a);
                        crossed.add(b);
                    }
                }
            }
        }

        /// добавляем вс
        for (Point point : points)
            if (!crossed.contains(point))
                single.add(point);

        // задача решена
        solved = true;
    }

    /**
     * Отмена решения задачи
     */
    public void cancel() {
        solved = false;
    }


    /**
     * Добавить случайные точки
     *
     * @param cnt кол-во случайных точек
     */
    public void addRandomPoints(int cnt) {
        // если создавать точки с полностью случайными координатами,
        // то вероятность того, что они совпадут крайне мала
        // поэтому нужно создать вспомогательную малую целочисленную ОСК
        // для получения случайной точки мы будем запрашивать случайную
        // координату этой решётки (их всего 30х30=900).
        // после нам останется только перевести координаты на решётке
        // в координаты СК задачи
        CoordinateSystem2i addGrid = new CoordinateSystem2i(30, 30);

        // повторяем заданное количество раз
        for (int i = 0; i < cnt; i++) {
            // получаем случайные координаты на решётке
            Vector2i gridPos = addGrid.getRandomCoords();
            // получаем координаты в СК задачи
            Vector2d pos = ownCS.getCoords(gridPos, addGrid);
            // сработает примерно в половине случаев
            addPoint(pos);
        }
    }


    /**
     * проверка, решена ли задача
     *
     * @return флаг
     */
    public boolean isSolved() {
        return solved;
    }

    public void setRandomTriangle() {
        Vector2d tA = ownCS.getRandomCoords();

    }


    Vector2d posA;
    Vector2d posB;


    public void addTrianglePoint(double x, double y) {
        if (posA == null) {
            posA = new Vector2d(x, y);
        } else if (posB == null) {
            posB = new Vector2d(x, y);
        } else {
            Vector2d posC = new Vector2d(x, y);
            triangle = new Triangle(posA, posB, posC);
            posA = null;
            posB = null;
        }
    }
}