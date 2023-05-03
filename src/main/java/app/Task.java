package app;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.github.humbleui.jwm.MouseButton;
import io.github.humbleui.skija.Canvas;
import io.github.humbleui.skija.Paint;
import io.github.humbleui.skija.RRect;
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
     * точки в ответе
     */
    Vector2d pos1_answer;
    Vector2d pos2_answer;

    /**
     * точки пересечения с треугольником прямой в ответе
     */
    Vector2d pos1_cross;
    Vector2d pos2_cross;



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
    public void renderTask(Canvas canvas, CoordinateSystem2i windowCS) {
        // Сохраняем последнюю СК
        lastWindowCS = windowCS;

        canvas.save();
        // создаём перо
        try (var p = new Paint()) {
            for (Point point : points) {
                // y-координату разворачиваем, потому что у СК окна ось y направлена вниз,
                // а в классическом представлении - вверх
                Vector2i windowPos = windowCS.getCoords(point.pos.x, point.pos.y, ownCS);
                canvas.drawRect(Rect.makeXYWH(windowPos.x - POINT_SIZE, windowPos.y - POINT_SIZE, POINT_SIZE * 2, POINT_SIZE * 2), p);
            }

            // рисуем треугольник
            if (triangle != null)
                triangle.render(canvas, windowCS, ownCS);
            if (solved && maxLenght != 0) {
                // рисуем прямую через точки в ответе
                Line lineAnswer = new Line(pos1_answer, pos2_answer);
                lineAnswer.render(canvas, windowCS, ownCS);

                canvas.drawRRect(RRect.makeXYWH((float) pos1_cross.x-2, (float) pos1_cross.y-2, POINT_SIZE * 2, POINT_SIZE * 2, 2), p);
                canvas.drawRRect(RRect.makeXYWH((float) pos2_cross.x-2, (float) pos2_cross.y-2, POINT_SIZE * 2, POINT_SIZE * 2, 2), p);
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
        // получаем положение точки на экране
        Vector2d taskPos = ownCS.getCoords(pos, lastWindowCS);
        Point pointByMouse = new Point (taskPos);
        // если левая кнопка мыши, добавляем точку
        if (mouseButton.equals(MouseButton.PRIMARY)) points.add(pointByMouse);
        // если правая кнопка мыши, добавляем треугольник по точкам на экране
        else if (mouseButton.equals(MouseButton.SECONDARY)) {
            // получаем положение 2-й точки на экране
            Vector2d taskPos1 = ownCS.getCoords(pos, lastWindowCS);
            Point pointByMouse1 = new Point (taskPos);
            // 3-й точки на экране
            Vector2d taskPos2 = ownCS.getCoords(pos, lastWindowCS);
            Point pointByMouse2 = new Point (taskPos);
            triangle = new Triangle(pointByMouse.getPos(), pointByMouse1.getPos(), pointByMouse2.getPos());
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

    /**
     * случайный треугольник
     */
    public void setRandomTriangle() {
        Vector2d tA = ownCS.getRandomCoords();
        Vector2d tB = ownCS.getRandomCoords();
        Vector2d tC = ownCS.getRandomCoords();
        triangle = new Triangle(tA, tB, tC);
    }



    /**
     * Добавить точку треугольника
     */

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


    // максимальная длина отрезка
    double maxLenght = 0;



    /**
     * решение задачи
     */
    public void solve() {
        //количество точек
        int numberPoints = points.size();
        // треугольник из списка
        Triangle triangleFromList = triangles.get(0);
        // получили треугольник
        Triangle triangle = triangleFromList.getTriangle();
        // вектора точек треугольника
        Vector2d A = triangle.pos1;
        Vector2d B = triangle.pos2;
        Vector2d C = triangle.pos3;
        // прямые, содержащие отрезки треугольника (заданы двумя точками)
        Line lineAB = new Line(A, B);
        Line lineBC = new Line(B, C);
        Line lineAC = new Line(A, C);

        // k и b этих прямых
        double k_AB = (A.y - B.y) / (A.x - B.x);
        double b_AB = B.y - k_AB * B.x;
        double k_BC = (B.y - C.y) / (B.x - C.x);
        double b_BC = C.y - k_BC * C.x;
        double k_AC = (A.y - C.y) / (A.x - C.x);
        double b_AC = A.y - k_AC * A.x;

        // длина отрезка
        double lenght = 0;


        for(int i = 0; i < numberPoints; i++) {
            for (int j = 0; j < numberPoints; j++) {
                // две точки
                Point pM = points.get(i);
                Point pN = points.get(j);
                // вектора этих двух точек
                Vector2d M = pM.getPos();
                Vector2d N = pN.getPos();
                // прямая через две точки
                Line line = new Line(M, N);
                // k и b данной прямой
                double k = (M.y - N.y) / (M.x - N.x);
                double b = M.y - k * M.x;
                // ыектора точек пересечения прямой с отрезками
                Vector2d CrossAB = null;
                Vector2d CrossBC = null;
                Vector2d CrossAC = null;


                // поиск точек пересечения с треугольником
                // поиск точки пересечения с АВ
                // если АВ непараллельна данной прямой
                if (k_AB != k) {
                    // координаты пересечения с прямой AB
                    double xCrossAB = (b_AB - b) / (k - k_AB);
                    double yCrossAB = xCrossAB * k + b;
                    // если точка пересечения на отрезке AB
                    if ((xCrossAB > A.x && xCrossAB < B.x) || (xCrossAB < A.x && xCrossAB > B.x)) {
                        CrossAB = new Vector2d(xCrossAB, yCrossAB);
                    }
                }
                // аналогично для BC
                // если BC непараллельна данной прямой
                if (k_BC != k) {
                    // координаты пересечения с прямой AB
                    double xCrossBC = (b_AB - b) / (k - k_AB);
                    double yCrossBC = xCrossBC * k + b;
                    // если точка пересечения на отрезке AB
                    if ((xCrossBC > B.x && xCrossBC < C.x) || (xCrossBC < B.x && xCrossBC > C.x)) {
                        CrossBC = new Vector2d(xCrossBC, yCrossBC);
                    }
                }
                // для AC
                // при двух предыдущих нулевых векторах прямая не пересекает треугольник
                // при двух предыдущих ненулевых векторах вершины отрезка внутри треугольника уже найдены
                if ((CrossAB != null && CrossBC == null) || (CrossAB == null && CrossBC != null) || (CrossAB == CrossBC)) {
                    if (k_AC != k) {
                        // координаты пересечения с прямой AB
                        double xCrossAC = (b_AB - b) / (k - k_AB);
                        double yCrossAC = xCrossAC * k + b;
                        // если точка пересечения на отрезке AB
                        if ((xCrossAC > A.x && xCrossAC < C.x) || (xCrossAC < A.x && xCrossAC > C.x)) {
                            CrossAC = new Vector2d(xCrossAC, yCrossAC);
                        }
                    }
                }


                // длина отрезка внутри треугольника
                if (CrossAB != null && CrossBC != null) {
                    // вектор отрезка внутри треугольника
                    Vector2d lineSegment = CrossAB.subtract(CrossBC);
                    lenght = lineSegment.length();
                    // если отрезок больше максимального
                    if (lenght > maxLenght) {
                        maxLenght = lenght;
                        pos1_answer = M;
                        pos2_answer = N;
                        pos1_cross = CrossAB;
                        pos2_cross = CrossBC;
                    }
                } else if (CrossBC != null && CrossAC != null) {
                    // вектор отрезка внутри треугольника
                    Vector2d lineSegment = CrossBC.subtract(CrossAC);
                    lenght = lineSegment.length();
                    // если отрезок больше максимального
                    if (lenght > maxLenght) {
                        maxLenght = lenght;
                        pos1_answer = M;
                        pos2_answer = N;
                        pos1_cross = CrossBC;
                        pos2_cross = CrossAC;
                    }
                } else if (CrossAB != null && CrossAC != null) {
                    Vector2d lineSegment = CrossAB.subtract(CrossAC);
                    lenght = lineSegment.length();
                    // если отрезок больше максимального
                    if (lenght > maxLenght) {
                        maxLenght = lenght;
                        pos1_answer = M;
                        pos2_answer = N;
                        pos1_cross = CrossAB;
                        pos2_cross = CrossAC;
                    }
                }
            }
        }
        if (maxLenght == 0) PanelLog.info("Нет решений");
        solved = true;
    }


}