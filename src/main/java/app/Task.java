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

    /**
     * треугольник
     */
    @Getter
    Triangle triangle;


    public Task(
            @JsonProperty("ownCS") CoordinateSystem2d ownCS,
            @JsonProperty("points") ArrayList<Point> points,
            @JsonProperty("lines") ArrayList<Line> lines

    ) {
        this.ownCS = ownCS;
        this.points = points;
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

                canvas.drawRRect(RRect.makeXYWH((float) pos1_cross.x - 2, (float) pos1_cross.y - 2, POINT_SIZE * 2, POINT_SIZE * 2, 2), p);
                canvas.drawRRect(RRect.makeXYWH((float) pos2_cross.x - 2, (float) pos2_cross.y - 2, POINT_SIZE * 2, POINT_SIZE * 2, 2), p);
            }
        }
        canvas.restore();
    }

    /**
     * точки треугольника
     */
    Vector2d posA;
    Vector2d posB;
    Vector2d posC;

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
        // выводим положение курсора на консоль
        System.out.println("click " + taskPos);
        Point pointByMouse = new Point(taskPos);
        // если левая кнопка мыши, добавляем точку
        if (mouseButton.equals(MouseButton.PRIMARY)) points.add(pointByMouse);
            // если правая кнопка мыши, добавляем треугольник по точкам на экране
        else if (mouseButton.equals(MouseButton.SECONDARY)) {

            // получаем положение 2-й точки на экране
            Vector2d taskPos1 = ownCS.getCoords(pos, lastWindowCS);
            if (posA == null) {
                posA = taskPos1;
            } else if (posB == null) {
                posB = taskPos1;
            } else {
                triangle = new Triangle(posA, posB, taskPos1);
                posA = null;
                posB = null;
            }
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

    Vector2d tPosA;
    Vector2d tPosB;


    public void addTrianglePoint(double x, double y) {
        if (tPosA == null) {
            tPosA = new Vector2d(x, y);
        } else if (tPosB == null) {
            tPosB = new Vector2d(x, y);
        } else {
            Vector2d posC = new Vector2d(x, y);
            triangle = new Triangle(tPosA, tPosB, posC);
            tPosA = null;
            tPosB = null;
        }
    }

    /**
     * Коэффициент а в общем уравнении прямой
     * @param line
     * @return а
     */
    public double get_line_a (Line line) {
        if (line.pos1.y == line.pos2.y) return 0;
        else return 1;
    }


    /**
     * Коэффициент b в общем уравнении прямой
     * @param line
     * @return b
     */
    public double get_line_b (Line line) {
        if (get_line_a(line) == 0) return 1;
        else return (line.pos1.x - line.pos2.x) / (line.pos2.y - line.pos1.y);
    }


    /**
     * Коэффициент c в общем уравнении прямой
     * @param line
     * @return c
     */
    public double get_line_c (Line line) {
        return (get_line_a(line) * line.pos1.x + get_line_b(line) * line.pos1.y) * -1;
    }

    /**
     * Точка пересечения двух прямых
     * @param line1
     * @param line2
     * @return vector
     */
    public Vector2d crossLine(Line line1, Line line2) {
        double a1 = get_line_a(line1);
        double b1 = get_line_b(line1);
        double c1 = get_line_c(line1);
        double a2 = get_line_a(line2);
        double b2 = get_line_b(line2);
        double c2 = get_line_c(line2);
        Vector2d vector = new Vector2d();
        if (a1 * b2 != a2 * b1 && a1 != 0) {
            vector.y = (a2 * c1 - a1 * c2) / (a1 * b2 - a2 * b1);
            vector.x = (c1 + b1 * vector.y) / a1 * -1;
        } else if (a1 * b2 != a2 * b1 && a1 == 0){
            vector.y = -1 * c1;
            if (a2 != 0) vector.x = (c1 * b2 - c2) / a2;
            else return null;
        } else if (a1 * b2 == a2 * b1) return null;
        return vector;
    }

    /**
     * Пересекает ли отрезок прямой
     */
    public boolean crossLineSegment(Line line1, Line line2, Vector2d pos_border_1, Vector2d pos_border_2) {
        Vector2d cross = crossLine(line1, line2);
        if (cross != null) {
            if ((cross.x > pos_border_1.x && cross.x < pos_border_2.x) || (cross.x < pos_border_1.x && cross.x > pos_border_2.x)) return true;
            else return false;
        } else return false;
    }

    /**
     * Если отрезок прямой внутри треугольника больше максимального
     */
    public void lenghtBiggerMax(Vector2d posM, Vector2d posN, Vector2d cross1, Vector2d cross2) {
        maxLenght = lenght;
        pos1_answer = posM;
        pos2_answer = posN;
        pos1_cross = cross1;
        pos2_cross = cross2;
    }


    /**
     * Максимальная длина отрезка внутри треугольника
     */
    double maxLenght = 0;

    /**
     * Длина отрезка внутри треугольника
     */
    double lenght = 0;
    /**
     * решение задачи
     */
    public void solve() {
        //количество точек
        int numberPoints = points.size();
        // вектора вершин треугольника
        posA = triangle.pos1;
        posB = triangle.pos2;
        posC = triangle.pos3;
        // прямые, содержащие отрезки треугольника (заданы двумя точками)
        Line lineAB = new Line(posA, posB);
        Line lineBC = new Line(posB, posC);
        Line lineAC = new Line(posA, posC);

        // перебор всех пар точек
        for(int i = 0; i < numberPoints; i++) {
            for(int j = 0; j < numberPoints; j++) {
                // две точки
                Vector2d posM = points.get(i).getPos();
                Vector2d posN = points.get(j).getPos();
                // прямая через них
                Line line = new Line(posM, posN);
                // точки пересечения со сторонами треугольника
                Vector2d crossAB = null;
                Vector2d crossBC = null;
                Vector2d crossAC = null;

                // смотрим, пересекает ли прямая отрезки треугольника
                if (crossLineSegment(line, lineAB, posA, posB)) {
                    crossAB = crossLine(line, lineAB);
                    System.out.println("crossAB " + crossAB.toString());
                }
                if (crossLineSegment(line, lineBC, posB, posC)) {
                    crossBC = crossLine(line, lineBC);
                    System.out.println("crossbc " + crossBC.toString());
                }
                if (crossLineSegment(line, lineAC, posA, posC)) {
                    crossAC = crossLine(line, lineAC);
                    System.out.println("crossAC " + crossAC.toString());
                }

                // вектор отрезка внутри треугольника
                Vector2d lineSegment = null;
                // если есть два пересечения со сторонами треугольника
                if (crossAB != null && crossBC != null) {
                    lineSegment = crossAB.subtract(crossBC);
                    lenght = lineSegment.length();
                    // если отрезок больше максимального
                    if (lenght > maxLenght) lenghtBiggerMax(posM, posN, crossAB, crossBC);
                    System.out.println("lenght " + lenght);
                    System.out.println("maxLenght " + maxLenght);
                    System.out.println("pos1_answer " + pos1_answer.toString());
                    System.out.println("pos2_answer " + pos2_answer.toString());
                    System.out.println("pos1_cross " + pos1_cross.toString());
                    System.out.println("pos2_cross " + pos2_cross.toString());
                    System.out.println();
                } else if (crossBC != null && crossAC != null) {
                    lineSegment = crossBC.subtract(crossAC);
                    lenght = lineSegment.length();
                    if (lenght > maxLenght) lenghtBiggerMax(posM, posN, crossBC, crossAC);
                    System.out.println("lenght " + lenght);
                    System.out.println("maxLenght " + maxLenght);
                    System.out.println("pos1_answer " + pos1_answer.toString());
                    System.out.println("pos2_answer " + pos2_answer.toString());
                    System.out.println("pos1_cross " + pos1_cross.toString());
                    System.out.println("pos2_cross " + pos2_cross.toString());
                    System.out.println();
                } else if (crossAB != null && crossAC != null) {
                    lineSegment = crossAB.subtract(crossAC);
                    lenght = lineSegment.length();
                    if (lenght > maxLenght) lenghtBiggerMax(posM, posN, crossAB, crossAC);
                    System.out.println("lenght " + lenght);
                    System.out.println("maxLenght " + maxLenght);
                    System.out.println("pos1_answer " + pos1_answer.toString());
                    System.out.println("pos2_answer " + pos2_answer.toString());
                    System.out.println("pos1_cross " + pos1_cross.toString());
                    System.out.println("pos2_cross " + pos2_cross.toString());
                    System.out.println();
                }
            }
        }
        solved = true;
    }
}