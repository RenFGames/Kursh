package yudxx.kursh.ui;

import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.ParallelTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.OverrunStyle;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import yudxx.kursh.KurshApp;
import yudxx.kursh.core.Fmt;
import yudxx.kursh.core.GameSave;
import yudxx.kursh.core.JsonLite;
import yudxx.kursh.i18n.T;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

public class MainView {

    private final Stage stage;
    private final DynamicIsland island = new DynamicIsland();

    private final ObservableList<SaveField> fields = FXCollections.observableArrayList();
    private final ObservableList<SaveField> shown = FXCollections.observableArrayList();
    private final ObservableList<String> fileNames = FXCollections.observableArrayList();
    private final List<File> saveFiles = new ArrayList<>();

    private StackPane rootLayer;
    private Toast toast;

    private TextField dirField;
    private TextField searchField;
    private TextField valueFilter;
    private ComboBox<String> valueOpBox;
    private ToggleButton typeInt;
    private ToggleButton typeFloat;
    private ToggleButton typeBool;
    private Node currentPageNode;
    private ComboBox<String> fileBox;
    private TableView<SaveField> table;
    private Label meta;

    private final List<Node> pages = new ArrayList<>();
    private ToggleGroup tabGroup;

    private File currentSave;
    private String originalText = "";
    private int changedCount;
    private final long startAt = System.currentTimeMillis();
    private Timeline runtimeTicker;

    public MainView(Stage stage) {
        this.stage = stage;
    }

    public Parent build() {
        VBox window = new VBox();
        window.getStyleClass().add("window");

        Node titleBar = buildTitleBar();
        Node body = buildBody();
        window.getChildren().addAll(titleBar, body);

        rootLayer = new StackPane(window);
        rootLayer.getStyleClass().add("window-root");
        rootLayer.setPadding(new Insets(18));

        Node islandNode = island.view();
        StackPane.setAlignment(islandNode, Pos.TOP_CENTER);
        StackPane.setMargin(islandNode, new Insets(9, 0, 0, 0));
        rootLayer.getChildren().add(islandNode);

        toast = new Toast(rootLayer);
        island.idle(T.t("未加载存档"));
        updateRuntime();
        runtimeTicker = new Timeline(new KeyFrame(Duration.seconds(1), e -> updateRuntime()));
        runtimeTicker.setCycleCount(Animation.INDEFINITE);
        runtimeTicker.play();

        playIntro(titleBar, body);
        return rootLayer;
    }

    public void dispose() {
        island.dispose();
        if (runtimeTicker != null) runtimeTicker.stop();
    }

    private void updateRuntime() {
        long seconds = Math.max(0, (System.currentTimeMillis() - startAt) / 1000);
        island.setTail(String.format("%02d:%02d", seconds / 60, seconds % 60));
    }

    private Node buildTitleBar() {
        HBox bar = new HBox(10);
        bar.getStyleClass().add("titlebar");
        bar.setPrefHeight(54);
        bar.setMinHeight(54);
        bar.setAlignment(Pos.CENTER_LEFT);

        Label brand = new Label(KurshApp.APP_NAME);
        brand.getStyleClass().add("brand-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button langBtn = new Button(T.isChinese() ? "EN" : "中");
        langBtn.getStyleClass().add("btn-lang");
        langBtn.setTooltip(new Tooltip(T.isChinese() ? "Switch to English" : "切换到中文"));
        langBtn.setOnAction(e -> {
            T.setChinese(!T.isChinese());
            KurshApp.reload();
        });

        StackPane aboutBtn = iconButton(Icons.INFO, 12);
        aboutBtn.setOnMouseClicked(e -> selectPage(1));
        Tooltip.install(aboutBtn, new Tooltip(T.t("关于 Kursh")));

        StackPane minBtn = iconButton(Icons.MIN, 11);
        minBtn.setOnMouseClicked(e -> stage.setIconified(true));

        StackPane closeBtn = iconButton(Icons.CLOSE, 11);
        closeBtn.getStyleClass().add("win-close");
        closeBtn.setOnMouseClicked(e -> {
            dispose();
            Platform.exit();
        });

        HBox actions = new HBox(3, langBtn, aboutBtn, minBtn, closeBtn);
        actions.setAlignment(Pos.CENTER_RIGHT);
        actions.setPadding(new Insets(0, 10, 0, 0));

        bar.getChildren().addAll(brand, spacer, actions);

        final double[] origin = new double[2];
        bar.setOnMousePressed(e -> {
            origin[0] = e.getSceneX();
            origin[1] = e.getSceneY();
        });
        bar.setOnMouseDragged(e -> {
            stage.setX(e.getScreenX() - origin[0]);
            stage.setY(e.getScreenY() - origin[1]);
        });
        return bar;
    }

    private Node buildBody() {
        VBox body = new VBox(10);
        body.setPadding(new Insets(62, 16, 14, 16));
        VBox.setVgrow(body, Priority.ALWAYS);

        pages.add(buildSavePanel());
        pages.add(buildAboutPanel());

        StackPane stack = new StackPane();
        for (int i = 0; i < pages.size(); i++) {
            Node page = pages.get(i);
            page.setVisible(i == 0);
            page.setManaged(i == 0);
            stack.getChildren().add(page);
        }
        currentPageNode = pages.get(0);
        VBox.setVgrow(stack, Priority.ALWAYS);

        String[] names = {T.t("存档"), T.t("关于")};
        HBox bar = new HBox(4);
        bar.getStyleClass().add("segmented");
        bar.setPadding(new Insets(3));
        bar.setMaxWidth(Region.USE_PREF_SIZE);
        ToggleGroup group = new ToggleGroup();
        tabGroup = group;
        for (int i = 0; i < names.length; i++) {
            ToggleButton button = new ToggleButton(names[i]);
            button.getStyleClass().add("seg-btn");
            button.setToggleGroup(group);
            button.setUserData(i);
            button.setSelected(i == 0);
            button.setFocusTraversable(false);
            bar.getChildren().add(button);
        }
        group.selectedToggleProperty().addListener((o, a, b) -> {
            if (b == null) {
                if (a != null) a.setSelected(true);
                return;
            }
            switchPage((Integer) b.getUserData());
        });

        HBox barRow = new HBox(bar);
        barRow.setAlignment(Pos.CENTER_LEFT);

        body.getChildren().addAll(barRow, stack);
        return body;
    }

    private void switchPage(int index) {
        Node next = pages.get(index);
        if (next == currentPageNode) return;
        Node prev = currentPageNode;
        currentPageNode = next;
        for (Node page : pages) {
            if (page != next && page != prev) {
                page.setVisible(false);
                page.setManaged(false);
            }
        }
        next.setVisible(true);
        next.setManaged(true);
        next.setOpacity(0);
        next.setTranslateX(22);
        FadeTransition enterFade = new FadeTransition(Duration.millis(210), next);
        enterFade.setToValue(1);
        TranslateTransition enterSlide = new TranslateTransition(Duration.millis(260), next);
        enterSlide.setToX(0);
        enterSlide.setInterpolator(Interpolator.EASE_OUT);
        new ParallelTransition(enterFade, enterSlide).play();

        if (prev != null) {
            FadeTransition exitFade = new FadeTransition(Duration.millis(140), prev);
            exitFade.setToValue(0);
            TranslateTransition exitSlide = new TranslateTransition(Duration.millis(200), prev);
            exitSlide.setToX(-14);
            ParallelTransition exit = new ParallelTransition(exitFade, exitSlide);
            exit.setOnFinished(e -> {
                prev.setVisible(false);
                prev.setManaged(false);
                prev.setOpacity(1);
                prev.setTranslateX(0);
            });
            exit.play();
        }
    }

    private void selectPage(int index) {
        if (tabGroup == null) return;
        List<javafx.scene.control.Toggle> toggles = new ArrayList<>(tabGroup.getToggles());
        if (index >= 0 && index < toggles.size()) toggles.get(index).setSelected(true);
    }

    private VBox buildSavePanel() {
        VBox card = new VBox(10);
        card.getStyleClass().add("card");
        card.setPadding(new Insets(14));
        VBox.setVgrow(card, Priority.ALWAYS);

        dirField = new TextField();
        dirField.setId("saveDir");
        dirField.getStyleClass().add("field");
        dirField.setPromptText(T.t("游戏根目录，例如 E:\\\\Games\\\\MyGame"));
        HBox.setHgrow(dirField, Priority.ALWAYS);
        dirField.setOnAction(e -> scanSaves());
        String lastDir = Preferences.userNodeForPackage(MainView.class).get("gameDir", "");
        if (!lastDir.isEmpty()) dirField.setText(lastDir);

        Button browse = new Button(T.t("浏览"));
        browse.getStyleClass().add("btn-ghost");
        browse.setOnAction(e -> chooseDir());

        Button scan = new Button(T.t("扫描存档"));
        scan.setId("saveScanBtn");
        scan.getStyleClass().add("btn-primary");
        scan.setOnAction(e -> scanSaves());

        HBox row1 = new HBox(8, dirField, browse, scan);
        row1.setAlignment(Pos.CENTER_LEFT);

        fileBox = new ComboBox<>(fileNames);
        fileBox.setId("saveFileBox");
        fileBox.setPrefWidth(320);
        fileBox.setPromptText(T.t("存档文件"));
        fileBox.setOnAction(e -> {
            int index = fileBox.getSelectionModel().getSelectedIndex();
            if (index >= 0 && index < saveFiles.size()) loadSave(saveFiles.get(index));
        });
        animateCombo(fileBox);

        Button backup = new Button(T.t("备份"));
        backup.getStyleClass().add("btn-ghost");
        backup.setOnAction(e -> doBackup());

        Button restore = new Button(T.t("还原"));
        restore.getStyleClass().add("btn-ghost");
        restore.setOnAction(e -> doRestore());

        Button revertEdits = new Button(T.t("撤回修改"));
        revertEdits.setId("revertBtn");
        revertEdits.getStyleClass().add("btn-ghost");
        revertEdits.setOnAction(e -> revertEdits());

        Button save = new Button(T.t("保存修改"));
        save.setId("saveWriteBtn");
        save.getStyleClass().add("btn-primary");
        save.setOnAction(e -> saveChanges());

        Region row2Spacer = new Region();
        HBox.setHgrow(row2Spacer, Priority.ALWAYS);

        HBox row2 = new HBox(8, fileBox, backup, restore, row2Spacer, revertEdits, save);
        row2.setAlignment(Pos.CENTER_LEFT);

        searchField = new TextField();
        searchField.setId("saveSearch");
        searchField.getStyleClass().add("field");
        searchField.setPromptText(T.t("按名称或数值搜索，例如 metal 或 3509"));
        searchField.setPrefWidth(230);
        searchField.textProperty().addListener((o, a, b) -> applyFilter());

        valueOpBox = new ComboBox<>(FXCollections.observableArrayList(
                T.t("全部"), "=", ">", "<", T.t("区间"), "≈"));
        valueOpBox.setId("valueOp");
        valueOpBox.setPrefWidth(78);
        valueOpBox.getSelectionModel().select(0);
        valueOpBox.setOnAction(e -> applyFilter());
        animateCombo(valueOpBox);

        valueFilter = new TextField();
        valueFilter.setId("valueFilter");
        valueFilter.getStyleClass().add("field");
        valueFilter.setPromptText(T.t("数值"));
        valueFilter.setPrefWidth(96);
        valueFilter.textProperty().addListener((o, a, b) -> applyFilter());

        Button resetFilter = tinyButton(T.t("重置"), "btn-chip");
        resetFilter.setOnAction(e -> {
            searchField.clear();
            valueFilter.clear();
            valueOpBox.getSelectionModel().select(0);
            applyFilter();
        });

        meta = new Label(T.t("未加载存档"));
        meta.getStyleClass().add("muted");

        Region row3Spacer = new Region();
        HBox.setHgrow(row3Spacer, Priority.ALWAYS);

        typeInt = typeToggle("int");
        typeFloat = typeToggle("float");
        typeBool = typeToggle("bool");

        meta.setMaxWidth(320);
        meta.setTextOverrun(OverrunStyle.ELLIPSIS);

        HBox row3 = new HBox(7, searchField, valueOpBox, valueFilter, resetFilter,
                typeInt, typeFloat, typeBool, row3Spacer, meta);
        row3.setAlignment(Pos.CENTER_LEFT);

        table = new TableView<>(shown);
        table.setId("saveTable");
        table.setEditable(true);
        table.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        table.setPlaceholder(new Label(T.t("选择游戏目录后点击扫描存档")));

        TableColumn<SaveField, String> cPath = new TableColumn<>(T.t("字段"));
        cPath.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().path));
        cPath.setPrefWidth(470);

        TableColumn<SaveField, String> cType = new TableColumn<>(T.t("类型"));
        cType.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().typeText()));
        cType.setPrefWidth(70);

        TableColumn<SaveField, String> cNow = new TableColumn<>(T.t("当前值"));
        cNow.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().text));
        cNow.setPrefWidth(160);

        TableColumn<SaveField, String> cNew = new TableColumn<>(T.t("新值"));
        cNew.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().newText));
        cNew.setCellFactory(TextFieldTableCell.forTableColumn());
        cNew.setOnEditCommit(e -> {
            e.getRowValue().newText = e.getNewValue();
            table.refresh();
            refreshChanged();
        });
        cNew.setPrefWidth(160);

        table.getColumns().addAll(cPath, cType, cNow, cNew);
        for (TableColumn<SaveField, ?> col : table.getColumns()) {
            col.setMinWidth(col.getPrefWidth());
        }

        VBox.setVgrow(table, Priority.ALWAYS);
        card.getChildren().addAll(row1, row2, row3, table);
        return card;
    }

    private VBox buildAboutPanel() {
        VBox page = new VBox();
        page.setAlignment(Pos.CENTER);

        Label name = new Label("Kursh");
        name.getStyleClass().add("about-page-title");

        Label sub = new Label(T.t("单机游戏存档编辑器"));
        sub.getStyleClass().add("about-sub");

        Label desc = new Label(T.t("自动定位 Unity 存档目录，解析 JSON 字段。改完保存，关掉游戏重新打开即可。"));
        desc.setWrapText(true);
        desc.setMaxWidth(420);
        desc.setPrefWidth(420);
        desc.setStyle("-fx-alignment: center;");
        desc.setAlignment(Pos.CENTER);
        desc.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        desc.getStyleClass().add("about-desc");

        javafx.scene.control.Separator sep = new javafx.scene.control.Separator();
        sep.setPrefWidth(280);
        sep.setMaxWidth(280);

        VBox author = new VBox(8);
        author.setMaxWidth(320);
        author.setPrefWidth(320);
        author.getChildren().addAll(
                aboutLine(T.t("作者"), "RenF Games", null),
                aboutLine("QQ", "2047752264", "2047752264"),
                aboutLine(T.t("微信"), "dll764", "dll764"));

        Label note = new Label(T.t("仅供单机游戏学习与研究使用，请勿用于联机游戏或商业用途。"));
        note.setWrapText(true);
        note.setMaxWidth(400);
        note.setAlignment(Pos.CENTER);
        note.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        note.getStyleClass().add("about-note");

        VBox box = new VBox(12);
        box.setAlignment(Pos.CENTER);
        box.getChildren().addAll(name, sub, desc, sep, author, note);

        page.getChildren().add(box);
        return page;
    }

    private HBox aboutLine(String key, String value, String copyText) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        Label keyLabel = new Label(key);
        keyLabel.getStyleClass().add("about-key");
        keyLabel.setMinWidth(42);
        Label valueLabel = new Label(value);
        valueLabel.getStyleClass().add("about-value");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        row.getChildren().addAll(keyLabel, valueLabel, spacer);
        if (copyText != null) {
            Button copy = tinyButton(T.t("复制"), "btn-chip");
            copy.setOnAction(e -> {
                javafx.scene.input.ClipboardContent content = new javafx.scene.input.ClipboardContent();
                content.putString(copyText);
                javafx.scene.input.Clipboard.getSystemClipboard().setContent(content);
                toast.ok(key + T.t(" 已复制"));
            });
            row.getChildren().add(copy);
        }
        return row;
    }

    private void animateCombo(ComboBox<?> combo) {
        if (combo == null) return;
        combo.showingProperty().addListener((o, a, b) -> {
            if (!b) return;
            Platform.runLater(() -> {
                Node popup = combo.lookup(".combo-box-popup");
                if (popup == null) return;
                popup.setOpacity(0);
                popup.setTranslateY(-6);
                FadeTransition fade = new FadeTransition(Duration.millis(140), popup);
                fade.setToValue(1);
                TranslateTransition slide = new TranslateTransition(Duration.millis(170), popup);
                slide.setToY(0);
                slide.setInterpolator(Interpolator.EASE_OUT);
                new ParallelTransition(fade, slide).play();
            });
        });
    }

    private void chooseDir() {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle(T.t("选择游戏根目录"));
        String current = dirField.getText();
        if (current != null && !current.isEmpty()) {
            File dir = new File(current);
            if (dir.isDirectory()) chooser.setInitialDirectory(dir);
        }
        File picked = chooser.showDialog(stage);
        if (picked == null) return;
        dirField.setText(picked.getAbsolutePath());
        scanSaves();
    }

    private void scanSaves() {
        String text = dirField.getText();
        if (text == null || text.trim().isEmpty()) {
            toast.warn(T.t("请先选择游戏目录"));
            return;
        }
        File dir = new File(text.trim());
        if (!dir.isDirectory()) {
            toast.err(T.t("目录不存在"));
            island.error(T.t("目录不存在"), dir.getAbsolutePath());
            return;
        }
        Preferences.userNodeForPackage(MainView.class).put("gameDir", dir.getAbsolutePath());
        island.busy(T.t("正在扫描存档"), dir.getName(), -1);

        File root = GameSave.findSaveRoot(dir);
        if (root == null) {
            island.error(T.t("没有找到该游戏的存档目录"), dir.getName());
            toast.err(T.t("没有找到该游戏的存档目录"));
            return;
        }
        List<File> found = GameSave.findSaveFiles(root);
        saveFiles.clear();
        saveFiles.addAll(found);
        fileNames.clear();
        for (File file : found) {
            String label = file.getName();
            try {
                label = root.toPath().relativize(file.toPath()).toString();
            } catch (Exception ignored) {
            }
            fileNames.add(label + "  ·  " + Fmt.bytes(file.length()));
        }
        if (found.isEmpty()) {
            island.error(T.t("没有找到可解析的存档文件"), root.getAbsolutePath());
            toast.warn(T.t("没有找到可解析的存档文件"));
            return;
        }
        fileBox.getSelectionModel().select(0);
        loadSave(found.get(0));
        toast.ok(T.t("找到 ") + found.size() + T.t(" 个存档文件"));
    }

    private void loadSave(File file) {
        currentSave = file;
        originalText = GameSave.read(file);
        changedCount = 0;
        if (!JsonLite.looksLikeJson(originalText)) {
            fields.clear();
            shown.clear();
            meta.setText(T.t("该文件不是 JSON 存档，暂不支持解析"));
            island.error(T.t("不支持的文件格式"), file.getName());
            return;
        }
        List<SaveField> rows = new ArrayList<>();
        try {
            for (JsonLite.Field field : JsonLite.fields(originalText)) {
                rows.add(new SaveField(field));
            }
        } catch (Exception e) {
            island.error(T.t("存档解析失败"), e.getMessage());
            toast.err(T.t("存档解析失败"));
            return;
        }
        fields.setAll(rows);
        applyFilter();
        updateMeta();
        table.setOpacity(0.35);
        FadeTransition fade = new FadeTransition(Duration.millis(240), table);
        fade.setToValue(1);
        fade.play();
        island.success(file.getName(), T.t("已解析 %d 个数值字段").formatted(rows.size()));
    }

    private ToggleButton typeToggle(String text) {
        ToggleButton button = new ToggleButton(text);
        button.getStyleClass().add("type-chip");
        button.setSelected(true);
        button.setFocusTraversable(false);
        button.setOnAction(e -> applyFilter());
        return button;
    }

    private void applyFilter() {
        String query = searchField == null || searchField.getText() == null
                ? "" : searchField.getText().trim().toLowerCase();
        boolean wantInt = typeInt == null || typeInt.isSelected();
        boolean wantFloat = typeFloat == null || typeFloat.isSelected();
        boolean wantBool = typeBool == null || typeBool.isSelected();
        boolean anyType = wantInt || wantFloat || wantBool;

        List<SaveField> out = new ArrayList<>();
        for (SaveField field : fields) {
            if (anyType) {
                if ("int".equals(field.kind) && !wantInt) continue;
                if ("number".equals(field.kind) && !wantFloat) continue;
                if ("bool".equals(field.kind) && !wantBool) continue;
            }
            if (!query.isEmpty()) {
                boolean byName = field.path.toLowerCase().contains(query);
                boolean byValue = field.text != null && field.text.toLowerCase().contains(query);
                if (!byName && !byValue) continue;
            }
            if (!matchValue(field)) continue;
            out.add(field);
        }
        shown.setAll(out);
        if (meta != null && currentSave != null) {
            meta.setText(currentSave.getName() + " · "
                    + T.t("已解析 %d 个数值字段").formatted(fields.size())
                    + " · " + T.t("已修改 %d 项").formatted(changedCount)
                    + (out.size() == fields.size() ? "" : " · " + T.t("显示 %d 项").formatted(out.size())));
        }
    }

    private boolean matchValue(SaveField field) {
        if (valueOpBox == null || valueFilter == null) return true;
        int op = valueOpBox.getSelectionModel().getSelectedIndex();
        String text = valueFilter.getText() == null ? "" : valueFilter.getText().trim();
        if (op <= 0 || text.isEmpty()) return true;
        double value;
        try {
            value = Double.parseDouble(field.text);
        } catch (Exception e) {
            if ("bool".equals(field.kind)) {
                value = "true".equals(field.text) ? 1 : 0;
            } else {
                return false;
            }
        }
        if (op == 4) {
            String[] parts = text.split("~");
            if (parts.length != 2) return true;
            try {
                double a = Double.parseDouble(parts[0].trim());
                double b = Double.parseDouble(parts[1].trim());
                return value >= Math.min(a, b) && value <= Math.max(a, b);
            } catch (Exception e) {
                return true;
            }
        }
        double target;
        try {
            target = Double.parseDouble(text);
        } catch (Exception e) {
            return true;
        }
        if (op == 1) return Math.abs(value - target) < 1e-9;
        if (op == 2) return value > target;
        if (op == 3) return value < target;
        if (op == 5) return Math.abs(value - target) <= Math.max(0.5, Math.abs(target) * 0.001);
        return true;
    }

    private void refreshChanged() {
        changedCount = 0;
        for (SaveField field : fields) {
            if (field.changed()) changedCount++;
        }
        updateMeta();
        if (changedCount > 0) {
            island.warn(T.t("已修改 %d 项").formatted(changedCount), T.t("点保存写入存档，改完重新打开游戏"));
        }
    }

    private void updateMeta() {
        if (currentSave == null) {
            meta.setText(T.t("未加载存档"));
            return;
        }
        meta.setText(currentSave.getName() + " · " + T.t("已解析 %d 个数值字段").formatted(fields.size())
                + " · " + T.t("已修改 %d 项").formatted(changedCount));
    }

    private void revertEdits() {
        int count = 0;
        for (SaveField field : fields) {
            if (field.changed()) {
                field.newText = field.text;
                count++;
            }
        }
        changedCount = 0;
        if (table != null) table.refresh();
        updateMeta();
        if (count == 0) {
            toast.info(T.t("没有改动需要保存"));
            return;
        }
        island.info(T.t("已撤回 %d 项修改").formatted(count), T.t("字段已恢复成存档里的原值"));
    }

    private void doBackup() {
        if (currentSave == null) {
            toast.warn(T.t("请先选择一个存档"));
            return;
        }
        File bak = GameSave.backup(currentSave);
        if (bak == null) {
            toast.err(T.t("备份失败"));
            island.error(T.t("备份失败"), T.t("目标文件可能不可写"));
            return;
        }
        toast.ok(T.t("已备份到 ") + bak.getName());
        island.info(T.t("已备份"), bak.getName());
    }

    private void doRestore() {
        if (currentSave == null) {
            toast.warn(T.t("请先选择一个存档"));
            return;
        }
        if (!GameSave.restore(currentSave)) {
            toast.err(T.t("没有找到备份文件"));
            island.error(T.t("还原失败"), T.t("没有找到备份文件"));
            return;
        }
        loadSave(currentSave);
        toast.ok(T.t("已还原到备份状态"));
        island.info(T.t("已还原"), currentSave.getName());
    }

    private void saveChanges() {
        if (currentSave == null) {
            toast.warn(T.t("请先扫描并选择存档"));
            return;
        }
        List<JsonLite.Field> edits = new ArrayList<>();
        for (SaveField field : fields) {
            if (!field.changed()) continue;
            String value = field.newText.trim();
            if ("bool".equals(field.kind)) {
                String lower = value.toLowerCase();
                if (!lower.equals("true") && !lower.equals("false")) {
                    island.error(T.t("值格式错误"), field.path);
                    toast.err(field.path + T.t(" 不是布尔值"));
                    return;
                }
                value = lower;
            } else {
                try {
                    Double.parseDouble(value);
                } catch (Exception e) {
                    island.error(T.t("值格式错误"), field.path);
                    toast.err(field.path + T.t(" 不是有效数字"));
                    return;
                }
            }
            edits.add(new JsonLite.Field(field.path, field.kind, value, field.start, field.end));
        }
        if (edits.isEmpty()) {
            toast.info(T.t("没有改动需要保存"));
            island.idle(T.t("没有改动"));
            return;
        }
        island.busy(T.t("正在写入存档"), currentSave.getName(), 0.6);
        File bak = GameSave.backup(currentSave);
        if (bak == null) {
            island.error(T.t("备份失败"), T.t("已取消写入"));
            toast.err(T.t("备份失败，已取消写入"));
            return;
        }
        String patched = JsonLite.patch(originalText, edits);
        if (!GameSave.write(currentSave, patched)) {
            island.error(T.t("写入失败"), T.t("文件可能被占用"));
            toast.err(T.t("写入失败，文件可能被占用"));
            return;
        }
        toast.ok(T.t("已保存 %d 项 · 关闭游戏后重新打开即可看到变化").formatted(edits.size()));
        loadSave(currentSave);
        island.success(T.t("已保存 %d 项").formatted(edits.size()),
                T.t("关闭游戏后重新打开即可看到变化"));
    }

    private StackPane iconButton(String content, double size) {
        StackPane box = new StackPane(Icons.of(content, Color.web("#8B84A6"), size));
        box.getStyleClass().add("win-btn");
        box.setPrefSize(30, 26);
        box.setMinSize(30, 26);
        box.setMaxSize(30, 26);
        box.setCursor(Cursor.HAND);
        box.setOnMousePressed(e -> e.consume());
        return box;
    }

    private Button tinyButton(String text, String styleClass) {
        Button button = new Button(text);
        button.getStyleClass().add(styleClass);
        button.setStyle("-fx-padding: 3 8; -fx-font-size: 10.5px;");
        return button;
    }

    private void playIntro(Node... nodes) {
        for (int i = 0; i < nodes.length; i++) {
            Node node = nodes[i];
            if (node == null) continue;
            node.setOpacity(0);
            node.setTranslateY(10);
            FadeTransition fade = new FadeTransition(Duration.millis(250), node);
            fade.setToValue(1);
            TranslateTransition slide = new TranslateTransition(Duration.millis(300), node);
            slide.setToY(0);
            slide.setInterpolator(Interpolator.EASE_OUT);
            ParallelTransition enter = new ParallelTransition(fade, slide);
            new SequentialTransition(new PauseTransition(Duration.millis(60L * i)), enter).play();
        }
    }
}
