package 삼성전자;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.StringTokenizer;

public class 루돌프의_반란 {
    static class Rudolph {
        int x;
        int y;

        public Rudolph(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    static class Santa implements Comparable<Santa> {
        int num;
        int x;
        int y;
        int score;
        boolean isAlive;
        boolean fainted;
        int faintedDay;

        public Santa(int num, int x, int y, int score, boolean isAlive, boolean fainted, int faintedDay) {
            this.num = num;
            this.x = x;
            this.y = y;
            this.score = score;
            this.isAlive = isAlive;
            this.fainted = fainted;
            this.faintedDay = faintedDay;
        }

        @Override
        public int compareTo(Santa otherSanta) {
            return this.num - otherSanta.num;
        }
    }

    static int N, M, P, C, D, deadCount;
    static Rudolph rudolph; // 루돌프
    static ArrayList<Santa> santaList; // 산타 목록
    static int[] scores;
    static int[][] board; // 격자
    static int[] dx8 = {-1, -1, 0, 1, 1, 1, 0, -1};
    static int[] dy8 = {0, 1, 1, 1, 0, -1, -1, -1};
    static int[] dx4 = {-1, 0, 1, 0};
    static int[] dy4 = {0, 1, 0, -1};

    private static void play() {
        rudolphMove();
        if (deadCount == P) return;

        santaMove();
        if (deadCount == P) return;

        getScores();
    }

    private static void getScores() {
        for (int p = 0; p < P; p++) {
            Santa santa = santaList.get(p);
            if (!santa.isAlive) continue;

            santa.score++;
            scores[p]++;

            if (santa.fainted) {
                santa.faintedDay--;

                if (santa.faintedDay == 0) {
                    santa.fainted = false;
                }
            }
        }
    }

    private static void rudolphMove() {
        int targetIndex = -1;
        int minDistance = Integer.MAX_VALUE;
        int santaX = -1;
        int santaY = -1;

        // 가장 가까운 거리의 산타 탐색
        for (Santa santa : santaList) {
            if (!santa.isAlive) continue; // 게임에서 탈락한 산타는 제외

            int calculatedDistance = getDistance(rudolph.x, rudolph.y, santa.x, santa.y);

            if (minDistance > calculatedDistance) {
                minDistance = calculatedDistance;
                santaX = santa.x;
                santaY = santa.y;
                targetIndex = santa.num;

            } else if (minDistance == calculatedDistance) {
                // 만약 가장 가까운 산타가 2명 이상이라면, r 좌표가 큰 산타를 향해 돌진합니다.
                // r이 동일한 경우, c 좌표가 큰 산타를 향해 돌진합니다.
                if ((santaX < santa.x) || (santaX == santa.x && santaY < santa.y)) {
                    santaX = santa.x;
                    santaY = santa.y;
                    targetIndex = santa.num;
                }
            }
        }

        // 가장 가까운 산타와 가장 가까워지는 방향으로 이동하는 방법을 찾기
        Santa targetSanta = santaList.get(targetIndex - 1);

        minDistance = Integer.MAX_VALUE;
        int nextMoveX = -1;
        int nextMoveY = -1;
        int nextDirection = -1;

        for (int i = 0; i < 8; i++) {
            int nextX = rudolph.x + dx8[i];
            int nextY = rudolph.y + dy8[i];

            int calculatedDistance = getDistance(nextX, nextY, targetSanta.x, targetSanta.y);

            if (calculatedDistance < minDistance) {
                minDistance = calculatedDistance;
                nextMoveX = nextX;
                nextMoveY = nextY;
                nextDirection = i;
            }
        }

        board[rudolph.x][rudolph.y] = 0; // 원래 루돌프가 있던 위치에 루돌프가 없어짐 (다른 칸으로 이동했으므로)

        if (board[nextMoveX][nextMoveY] > 0) { // 루돌프가 움직여서 그 자리에 산타가 있어서 충돌한 경우
            collideWithSanta(targetSanta, C, true, nextDirection, true);
        }

        rudolph.x = nextMoveX;
        rudolph.y = nextMoveY;

        board[rudolph.x][rudolph.y] = -1;
    }

    private static void collideWithSanta(Santa targetSanta, int score, boolean getScore, int pushDirection, boolean pushedByRudolph) {
        if (getScore) {
            targetSanta.score += score;
            scores[targetSanta.num - 1] += score;
        }

        int santaMoveX = targetSanta.x;
        if (pushedByRudolph) {
            santaMoveX += dx8[pushDirection] * score;
        } else {
            santaMoveX += dx4[pushDirection] * score;
        }

        int santaMoveY = targetSanta.y;
        if (pushedByRudolph) {
            santaMoveY += dy8[pushDirection] * score;
        } else {
            santaMoveY += dy4[pushDirection] * score;
        }

        if (checkOutOfRange(santaMoveX, santaMoveY)) { // 만약 밀려난 위치가 게임판 밖이라면 산타는 게임에서 탈락
            targetSanta.isAlive = false;
            deadCount++;
            return;
        }

        if (board[santaMoveX][santaMoveY] > 0) { // 만약 밀려난 칸에 다른 산타가 있는 경우 상호작용이 발생
            // 산타는 충돌 후 착지하게 되는 칸에 다른 산타가 있다면 그 산타는 1칸 해당 방향으로 밀려나게 됨
            Santa hitSanta = santaList.get(board[santaMoveX][santaMoveY] - 1);
            collideWithSanta(hitSanta, 1, false, pushDirection, pushedByRudolph);
        }

        // 만약 밀려난 칸에 다른 산타가 없는 경우
        board[santaMoveX][santaMoveY] = targetSanta.num;
        targetSanta.x = santaMoveX;
        targetSanta.y = santaMoveY;

        if (getScore) {
            targetSanta.fainted = true;
            targetSanta.faintedDay = 2;
        }
    }

    private static void santaMove() {
        for (Santa santa : santaList) {
            if (!santa.isAlive || santa.fainted) continue; // 기절했거나 이미 게임에서 탈락한 산타는 움직일 수 없음

            int minDistance = Integer.MAX_VALUE;
            int santaX = santa.x;
            int santaY = santa.y;
            int currentDistance = getDistance(rudolph.x, rudolph.y, santaX, santaY); // 산타의 현재 위치 ~ 루돌프의 현재 위치
            int moveDirection = -1;

            // 산타는 루돌프에게 가장 가까워지는 방향으로 1칸 이동
            for (int d = 0; d < 4; d++) {
                int nextX = santaX + dx4[d];
                int nextY = santaY + dy4[d];

                // 게임판 밖으로는 움직일 수 없음 || 다른 산타가 있는 칸으로는 움직일 수 없음
                if (checkOutOfRange(nextX, nextY) ||
                        (board[nextX][nextY] > 0 && board[nextX][nextY] != santa.num)) continue;

                int calculatedDistance = getDistance(rudolph.x, rudolph.y, nextX, nextY);

                // 산타가 상하좌우로 인접한 4방향 중 한 곳으로 움직일 때,
                // 가장 가까워질 수 있는 방향이 여러 개라면, 상우하좌 우선순위에 맞춰 움직인다.
                if (minDistance > calculatedDistance && currentDistance > calculatedDistance) {
                    minDistance = calculatedDistance;
                    moveDirection = d;
                }
            }

            // 만약 루돌프로부터 가까워질 수 있는 방법이 없다면 산타는 움직이지 않음
            if (moveDirection == -1) continue;

            int nextMoveX = santaX + dx4[moveDirection];
            int nextMoveY = santaY + dy4[moveDirection];
            santa.x = nextMoveX;
            santa.y = nextMoveY;

            board[santaX][santaY] = 0; // 원래 산타가 있던 위치에 산타가 없어짐 (다른 칸으로 이동했으므로)

            if (board[nextMoveX][nextMoveY] == -1) { // 이동하려는 위치에 루돌프가 있어서 충돌하는 경우
                // 산타는 자신이 이동해온 반대 방향으로 D 칸 만큼 밀려남
                moveDirection = (moveDirection + 2) % 4;
                collideWithSanta(santa, D, true, moveDirection, false);
            } else { // 이동하려는 위치에 다른 산타가 없는 경우
                board[nextMoveX][nextMoveY] = santa.num;
            }
        }
    }

    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        StringTokenizer st = new StringTokenizer(br.readLine());

        N = Integer.parseInt(st.nextToken()); // 격자의 너비, 높이
        M = Integer.parseInt(st.nextToken()); // 게임의 총 턴 수
        P = Integer.parseInt(st.nextToken()); // 산타의 수
        C = Integer.parseInt(st.nextToken()); // 루돌프의 힘
        D = Integer.parseInt(st.nextToken()); // 산타의 힘

        board = new int[N + 1][N + 1];
        scores = new int[P + 1];

        st = new StringTokenizer(br.readLine());

        int rudolphStartX = Integer.parseInt(st.nextToken());
        int rudolphStartY = Integer.parseInt(st.nextToken());
        rudolph = new Rudolph(rudolphStartX, rudolphStartY);

        santaList = new ArrayList<>();

        for (int i = 0; i < P; i++) {
            st = new StringTokenizer(br.readLine());
            int santaNum = Integer.parseInt(st.nextToken());
            int santaStartX = Integer.parseInt(st.nextToken());
            int santaStartY = Integer.parseInt(st.nextToken());

            santaList.add(new Santa(santaNum, santaStartX, santaStartY, 0, true, false, 0));
            board[santaStartX][santaStartY] = santaNum;
        }

        Collections.sort(santaList);

        // 게임 실행
        while (M-- > 0) {
            play();
            if (deadCount == P) break;
        }

        for (Santa santa : santaList) {
            System.out.print(santa.score + " ");
        }
        System.out.println();
    }

    private static int getDistance(int rudolphX, int rudolphY, int santaX, int santaY) {
        return (int) Math.pow(rudolphX - santaX, 2) + (int) Math.pow(rudolphY - santaY, 2);
    }

    private static boolean checkOutOfRange(int x, int y) {
        return x < 1 || x > N || y < 1 || y > N;
    }
}
