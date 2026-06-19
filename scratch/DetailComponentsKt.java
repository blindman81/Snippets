package com.android.snippets.ui;

import android.graphics.Typeface;
import android.view.View;
import androidx.compose.animation.AnimatedContentKt;
import androidx.compose.animation.AnimatedContentScope;
import androidx.compose.animation.AnimatedContentTransitionScope;
import androidx.compose.animation.AnimatedVisibilityScope;
import androidx.compose.animation.ContentTransform;
import androidx.compose.animation.EnterExitTransitionKt;
import androidx.compose.animation.EnterTransition;
import androidx.compose.animation.ExitTransition;
import androidx.compose.animation.core.AnimateAsStateKt;
import androidx.compose.animation.core.AnimationSpec;
import androidx.compose.animation.core.AnimationSpecKt;
import androidx.compose.animation.core.DurationBasedAnimationSpec;
import androidx.compose.animation.core.EasingKt;
import androidx.compose.animation.core.FiniteAnimationSpec;
import androidx.compose.animation.core.InfiniteRepeatableSpec;
import androidx.compose.animation.core.InfiniteTransition;
import androidx.compose.animation.core.InfiniteTransitionKt;
import androidx.compose.animation.core.RepeatMode;
import androidx.compose.animation.core.VectorConvertersKt;
import androidx.compose.foundation.BackgroundKt;
import androidx.compose.foundation.BorderKt;
import androidx.compose.foundation.BorderStroke;
import androidx.compose.foundation.BorderStrokeKt;
import androidx.compose.foundation.ClickableKt;
import androidx.compose.foundation.Indication;
import androidx.compose.foundation.OverscrollEffect;
import androidx.compose.foundation.ScrollKt;
import androidx.compose.foundation.gestures.FlingBehavior;
import androidx.compose.foundation.interaction.InteractionSourceKt;
import androidx.compose.foundation.interaction.MutableInteractionSource;
import androidx.compose.foundation.layout.Arrangement;
import androidx.compose.foundation.layout.AspectRatioKt;
import androidx.compose.foundation.layout.BoxKt;
import androidx.compose.foundation.layout.BoxScope;
import androidx.compose.foundation.layout.BoxScopeInstance;
import androidx.compose.foundation.layout.ColumnKt;
import androidx.compose.foundation.layout.ColumnScope;
import androidx.compose.foundation.layout.ColumnScopeInstance;
import androidx.compose.foundation.layout.FlowLayoutKt;
import androidx.compose.foundation.layout.FlowRowScope;
import androidx.compose.foundation.layout.OffsetKt;
import androidx.compose.foundation.layout.PaddingKt;
import androidx.compose.foundation.layout.PaddingValues;
import androidx.compose.foundation.layout.RowKt;
import androidx.compose.foundation.layout.RowScope;
import androidx.compose.foundation.layout.RowScopeInstance;
import androidx.compose.foundation.layout.SizeKt;
import androidx.compose.foundation.layout.SpacerKt;
import androidx.compose.foundation.layout.WindowInsetsPadding_androidKt;
import androidx.compose.foundation.lazy.grid.GridCells;
import androidx.compose.foundation.lazy.grid.LazyGridDslKt;
import androidx.compose.foundation.lazy.grid.LazyGridItemScope;
import androidx.compose.foundation.lazy.grid.LazyGridScope;
import androidx.compose.foundation.lazy.grid.LazyGridState;
import androidx.compose.foundation.shape.GenericShape;
import androidx.compose.foundation.shape.RoundedCornerShape;
import androidx.compose.foundation.shape.RoundedCornerShapeKt;
import androidx.compose.foundation.text.KeyboardActionScope;
import androidx.compose.foundation.text.KeyboardActions;
import androidx.compose.foundation.text.KeyboardOptions;
import androidx.compose.foundation.text.TextAutoSize;
import androidx.compose.foundation.text.selection.TextSelectionColors;
import androidx.compose.material.icons.Icons;
import androidx.compose.material.icons.Icons.AutoMirrored.Filled;
import androidx.compose.material.icons.automirrored.filled.ArrowBackKt;
import androidx.compose.material.icons.filled.AddKt;
import androidx.compose.material.icons.filled.CloseKt;
import androidx.compose.material.icons.filled.ExpandMoreKt;
import androidx.compose.material.icons.filled.FavoriteBorderKt;
import androidx.compose.material.icons.filled.FavoriteKt;
import androidx.compose.material.icons.filled.FileDownloadKt;
import androidx.compose.material.icons.filled.PaletteKt;
import androidx.compose.material.icons.filled.PhotoKt;
import androidx.compose.material.icons.filled.ShareKt;
import androidx.compose.material.icons.filled.ShuffleKt;
import androidx.compose.material3.AndroidMenu_androidKt;
import androidx.compose.material3.BottomSheetDefaults;
import androidx.compose.material3.ButtonDefaults;
import androidx.compose.material3.ButtonElevation;
import androidx.compose.material3.ButtonGroupDefaults;
import androidx.compose.material3.ButtonGroupKt;
import androidx.compose.material3.ButtonGroupScope;
import androidx.compose.material3.ButtonKt;
import androidx.compose.material3.ChipColors;
import androidx.compose.material3.ChipElevation;
import androidx.compose.material3.ChipKt;
import androidx.compose.material3.DividerKt;
import androidx.compose.material3.IconButtonColors;
import androidx.compose.material3.IconButtonKt;
import androidx.compose.material3.IconKt;
import androidx.compose.material3.MaterialTheme;
import androidx.compose.material3.MenuItemColors;
import androidx.compose.material3.ModalBottomSheetKt;
import androidx.compose.material3.ModalBottomSheetProperties;
import androidx.compose.material3.MotionScheme;
import androidx.compose.material3.OutlinedTextFieldDefaults;
import androidx.compose.material3.OutlinedTextFieldKt;
import androidx.compose.material3.SheetState;
import androidx.compose.material3.SuggestionChipDefaults;
import androidx.compose.material3.SurfaceKt;
import androidx.compose.material3.TextFieldColors;
import androidx.compose.material3.TextKt;
import androidx.compose.runtime.Applier;
import androidx.compose.runtime.Composable;
import androidx.compose.runtime.ComposableTarget;
import androidx.compose.runtime.ComposablesKt;
import androidx.compose.runtime.Composer;
import androidx.compose.runtime.ComposerKt;
import androidx.compose.runtime.CompositionLocal;
import androidx.compose.runtime.CompositionLocalMap;
import androidx.compose.runtime.EffectsKt;
import androidx.compose.runtime.IntState;
import androidx.compose.runtime.MutableIntState;
import androidx.compose.runtime.MutableState;
import androidx.compose.runtime.RecomposeScopeImplKt;
import androidx.compose.runtime.ScopeUpdateScope;
import androidx.compose.runtime.SnapshotIntStateKt;
import androidx.compose.runtime.SnapshotMutationPolicy;
import androidx.compose.runtime.SnapshotStateKt;
import androidx.compose.runtime.State;
import androidx.compose.runtime.Updater;
import androidx.compose.runtime.internal.ComposableLambdaKt;
import androidx.compose.ui.Alignment;
import androidx.compose.ui.ComposedModifierKt;
import androidx.compose.ui.Modifier;
import androidx.compose.ui.draw.ClipKt;
import androidx.compose.ui.geometry.Size;
import androidx.compose.ui.graphics.AndroidPath_androidKt;
import androidx.compose.ui.graphics.Color;
import androidx.compose.ui.graphics.ColorKt;
import androidx.compose.ui.graphics.GraphicsLayerModifierKt;
import androidx.compose.ui.graphics.GraphicsLayerScope;
import androidx.compose.ui.graphics.Path;
import androidx.compose.ui.graphics.RectangleShapeKt;
import androidx.compose.ui.graphics.Shadow;
import androidx.compose.ui.graphics.Shape;
import androidx.compose.ui.graphics.drawscope.DrawStyle;
import androidx.compose.ui.graphics.vector.ImageVector;
import androidx.compose.ui.input.pointer.PointerInputEventHandler;
import androidx.compose.ui.input.pointer.SuspendingPointerInputFilterKt;
import androidx.compose.ui.layout.MeasurePolicy;
import androidx.compose.ui.node.ComposeUiNode;
import androidx.compose.ui.platform.AndroidCompositionLocals_androidKt;
import androidx.compose.ui.res.VectorResources_androidKt;
import androidx.compose.ui.semantics.Role;
import androidx.compose.ui.text.PlatformTextStyle;
import androidx.compose.ui.text.TextStyle;
import androidx.compose.ui.text.font.AndroidTypeface_androidKt;
import androidx.compose.ui.text.font.FontFamily;
import androidx.compose.ui.text.font.FontStyle;
import androidx.compose.ui.text.font.FontSynthesis;
import androidx.compose.ui.text.font.FontWeight;
import androidx.compose.ui.text.input.ImeAction;
import androidx.compose.ui.text.input.PlatformImeOptions;
import androidx.compose.ui.text.input.VisualTransformation;
import androidx.compose.ui.text.intl.LocaleList;
import androidx.compose.ui.text.style.BaselineShift;
import androidx.compose.ui.text.style.LineHeightStyle;
import androidx.compose.ui.text.style.TextAlign;
import androidx.compose.ui.text.style.TextDecoration;
import androidx.compose.ui.text.style.TextGeometricTransform;
import androidx.compose.ui.text.style.TextIndent;
import androidx.compose.ui.text.style.TextMotion;
import androidx.compose.ui.unit.Dp;
import androidx.compose.ui.unit.LayoutDirection;
import androidx.compose.ui.unit.TextUnit;
import androidx.compose.ui.unit.TextUnitKt;
import androidx.compose.ui.window.AndroidDialog_androidKt;
import androidx.compose.ui.window.DialogProperties;
import androidx.graphics.shapes.CornerRounding;
import androidx.graphics.shapes.RoundedPolygon;
import androidx.graphics.shapes.ShapesKt;
import androidx.graphics.shapes.Shapes_androidKt;
import com.android.snippets.R.drawable;
import com.android.snippets.model.Photo;
import com.android.snippets.ui.DetailComponentsKt.AddSnippetsModal.lambda.158.lambda.157.lambda.152.lambda.151.lambda.150.lambda.149.lambda.139.lambda.138..inlined.items.default.1;
import com.android.snippets.ui.components.AnimatedCookieButtonKt;
import com.android.snippets.ui.components.SelectionKt;
import com.android.snippets.ui.components.SplitButtonKt;
import com.android.snippets.ui.util.DistributionMath;
import com.android.snippets.ui.util.ModifierExtKt;
import com.android.snippets.viewmodel.SnippetStyle;
import com.android.snippets.viewmodel.SnippetsViewModel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import kotlin.Metadata;
import kotlin.Unit;
import kotlin.collections.CollectionsKt;
import kotlin.coroutines.Continuation;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.functions.Function2;
import kotlin.jvm.functions.Function3;
import kotlin.jvm.functions.Function4;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.SourceDebugExtension;
import kotlin.random.Random;
import kotlin.random.RandomKt;
import kotlin.ranges.RangesKt;
import kotlin.reflect.KProperty;
import kotlin.text.StringsKt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Metadata(
   mv = {2, 2, 0},
   k = 2,
   xi = 48,
   d1 = {"\u0000\u0092\u0001\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u000f\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0007\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0010 \n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\b\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\n\n\u0002\u0018\u0002\n\u0002\b\u0007\u001a÷\u0001\u0010\u0004\u001a\u00020\u00052\u0006\u0010\u0006\u001a\u00020\u00072\u0006\u0010\b\u001a\u00020\t2\f\u0010\n\u001a\b\u0012\u0004\u0012\u00020\u00050\u000b2\b\b\u0002\u0010\f\u001a\u00020\r2\b\b\u0002\u0010\u000e\u001a\u00020\r2\u000e\b\u0002\u0010\u000f\u001a\b\u0012\u0004\u0012\u00020\u00050\u000b2\u0006\u0010\u0010\u001a\u00020\r2\f\u0010\u0011\u001a\b\u0012\u0004\u0012\u00020\u00050\u000b2\f\u0010\u0012\u001a\b\u0012\u0004\u0012\u00020\u00050\u000b2\f\u0010\u0013\u001a\b\u0012\u0004\u0012\u00020\u00050\u000b2\f\u0010\u0014\u001a\b\u0012\u0004\u0012\u00020\u00050\u000b2\f\u0010\u0015\u001a\b\u0012\u0004\u0012\u00020\u00050\u000b2\b\b\u0002\u0010\u0016\u001a\u00020\r2\u000e\b\u0002\u0010\u0017\u001a\b\u0012\u0004\u0012\u00020\u00050\u000b2\b\b\u0002\u0010\u0018\u001a\u00020\r2\u000e\b\u0002\u0010\u0019\u001a\b\u0012\u0004\u0012\u00020\u00050\u000b2\b\b\u0002\u0010\u001a\u001a\u00020\r2\u000e\b\u0002\u0010\u001b\u001a\b\u0012\u0004\u0012\u00020\u00050\u000b2\n\b\u0002\u0010\u001c\u001a\u0004\u0018\u00010\u001dH\u0007¢\u0006\u0002\u0010\u001e\u001a!\u0010\u001f\u001a\u00020\u00052\b\b\u0002\u0010 \u001a\u00020!2\b\b\u0002\u0010\"\u001a\u00020#H\u0007¢\u0006\u0002\u0010$\u001a'\u0010%\u001a\u00020&2\u0006\u0010'\u001a\u00020(2\u0006\u0010)\u001a\u00020&2\b\b\u0002\u0010*\u001a\u00020\rH\u0007¢\u0006\u0002\u0010+\u001aM\u0010,\u001a\u00020\u00052\u0006\u0010-\u001a\u00020.2\u0006\u0010/\u001a\u0002002\u0006\u00101\u001a\u0002002\u000e\b\u0002\u00102\u001a\b\u0012\u0004\u0012\u000200032\n\b\u0002\u00104\u001a\u0004\u0018\u0001002\n\b\u0002\u00105\u001a\u0004\u0018\u00010(H\u0007¢\u0006\u0002\u00106\u001a-\u00107\u001a\u00020\u00052\u0006\u00108\u001a\u0002092\u0006\u0010:\u001a\u00020;2\f\u0010<\u001a\b\u0012\u0004\u0012\u00020\u00050\u000bH\u0007¢\u0006\u0004\b=\u0010>\u001a-\u0010?\u001a\u00020\u00052\u0006\u00108\u001a\u0002092\u0006\u0010@\u001a\u00020;2\f\u0010<\u001a\b\u0012\u0004\u0012\u00020\u00050\u000bH\u0007¢\u0006\u0004\bA\u0010>\u001a?\u0010B\u001a\u00020\u00052\u0006\u0010\u0006\u001a\u00020\u00072\u0012\u0010C\u001a\u000e\u0012\u0004\u0012\u00020.\u0012\u0004\u0012\u00020\u00050D2\f\u0010E\u001a\b\u0012\u0004\u0012\u00020\u00050\u000b2\u0006\u0010\b\u001a\u00020\tH\u0007¢\u0006\u0002\u0010F\u001aK\u0010G\u001a\u00020\u00052\u0006\u0010\u0006\u001a\u00020\u00072\u001e\u0010\u0011\u001a\u001a\u0012\u0004\u0012\u00020.\u0012\u0004\u0012\u000200\u0012\u0004\u0012\u00020(\u0012\u0004\u0012\u00020\u00050H2\f\u0010E\u001a\b\u0012\u0004\u0012\u00020\u00050\u000b2\u0006\u0010\b\u001a\u00020\tH\u0007¢\u0006\u0002\u0010I\u001a7\u0010J\u001a\u00020\u00052\u0006\u0010K\u001a\u00020L2\f\u0010M\u001a\b\u0012\u0004\u0012\u00020\u00050\u000b2\u0012\u0010N\u001a\u000e\u0012\u0004\u0012\u00020\r\u0012\u0004\u0012\u00020\u00050DH\u0007¢\u0006\u0002\u0010O\u001a=\u0010P\u001a\u00020\u00052\u0006\u0010Q\u001a\u00020.2\u0006\u0010R\u001a\u00020\r2\u0006\u0010S\u001a\u00020\r2\f\u0010<\u001a\b\u0012\u0004\u0012\u00020\u00050\u000b2\b\b\u0002\u0010 \u001a\u00020!H\u0007¢\u0006\u0002\u0010T\"\u0011\u0010\u0000\u001a\u00020\u0001¢\u0006\b\n\u0000\u001a\u0004\b\u0002\u0010\u0003¨\u0006U²\u0006\n\u0010V\u001a\u00020WX\u008a\u0084\u0002²\u0006\u0010\u0010X\u001a\b\u0012\u0004\u0012\u00020.03X\u008a\u008e\u0002²\u0006\n\u0010Y\u001a\u00020#X\u008a\u008e\u0002²\u0006\n\u0010Z\u001a\u00020#X\u008a\u0084\u0002²\u0006\n\u0010-\u001a\u00020.X\u008a\u008e\u0002²\u0006\f\u0010[\u001a\u0004\u0018\u000100X\u008a\u008e\u0002²\u0006\n\u0010\\\u001a\u00020(X\u008a\u008e\u0002²\u0006\n\u0010]\u001a\u000200X\u008a\u008e\u0002²\u0006\n\u0010^\u001a\u00020\rX\u008a\u008e\u0002"},
   d2 = {"CookieShape", "Landroidx/compose/foundation/shape/GenericShape;", "getCookieShape", "()Landroidx/compose/foundation/shape/GenericShape;", "DetailTopBar", "", "photo", "Lcom/android/snippets/model/Photo;", "viewModel", "Lcom/android/snippets/viewmodel/SnippetsViewModel;", "onBack", "Lkotlin/Function0;", "isSpinning", "", "isScrolled", "onPhotoThumbnailClick", "hasSnippets", "onAdd", "onDownload", "onEdit", "onShare", "onDelete", "isFavorite", "onToggleFavorite", "isPublic", "onTogglePublic", "hasLocationLink", "onAddLinkClick", "animatedVisibilityScope", "Landroidx/compose/animation/AnimatedVisibilityScope;", "(Lcom/android/snippets/model/Photo;Lcom/android/snippets/viewmodel/SnippetsViewModel;Lkotlin/jvm/functions/Function0;ZZLkotlin/jvm/functions/Function0;ZLkotlin/jvm/functions/Function0;Lkotlin/jvm/functions/Function0;Lkotlin/jvm/functions/Function0;Lkotlin/jvm/functions/Function0;Lkotlin/jvm/functions/Function0;ZLkotlin/jvm/functions/Function0;ZLkotlin/jvm/functions/Function0;ZLkotlin/jvm/functions/Function0;Landroidx/compose/animation/AnimatedVisibilityScope;Landroidx/compose/runtime/Composer;III)V", "SwipePrompt", "modifier", "Landroidx/compose/ui/Modifier;", "alphaValue", "", "(Landroidx/compose/ui/Modifier;FLandroidx/compose/runtime/Composer;II)V", "getSnippetTextStyle", "Landroidx/compose/ui/text/TextStyle;", "style", "Lcom/android/snippets/viewmodel/SnippetStyle;", "base", "isCloud", "(Lcom/android/snippets/viewmodel/SnippetStyle;Landroidx/compose/ui/text/TextStyle;ZLandroidx/compose/runtime/Composer;II)Landroidx/compose/ui/text/TextStyle;", "CloudSnippetItem", "text", "", "index", "", "totalCount", "photoColors", "", "forcedColor", "forcedStyle", "(Ljava/lang/String;IILjava/util/List;Ljava/lang/Integer;Lcom/android/snippets/viewmodel/SnippetStyle;Landroidx/compose/runtime/Composer;II)V", "ActionIcon", "icon", "Landroidx/compose/ui/graphics/vector/ImageVector;", "tint", "Landroidx/compose/ui/graphics/Color;", "onClick", "ActionIcon-iJQMabo", "(Landroidx/compose/ui/graphics/vector/ImageVector;JLkotlin/jvm/functions/Function0;Landroidx/compose/runtime/Composer;I)V", "HeaderActionButton", "color", "HeaderActionButton-iJQMabo", "CurrentSnippetsModal", "onRemove", "Lkotlin/Function1;", "onClose", "(Lcom/android/snippets/model/Photo;Lkotlin/jvm/functions/Function1;Lkotlin/jvm/functions/Function0;Lcom/android/snippets/viewmodel/SnippetsViewModel;Landroidx/compose/runtime/Composer;I)V", "AddSnippetsModal", "Lkotlin/Function3;", "(Lcom/android/snippets/model/Photo;Lkotlin/jvm/functions/Function3;Lkotlin/jvm/functions/Function0;Lcom/android/snippets/viewmodel/SnippetsViewModel;Landroidx/compose/runtime/Composer;I)V", "CanvasBackgroundDialog", "action", "Lcom/android/snippets/ui/CanvasAction;", "onDismiss", "onConfirm", "(Lcom/android/snippets/ui/CanvasAction;Lkotlin/jvm/functions/Function0;Lkotlin/jvm/functions/Function1;Landroidx/compose/runtime/Composer;I)V", "CanvasOptionCard", "title", "isDark", "isSelected", "(Ljava/lang/String;ZZLkotlin/jvm/functions/Function0;Landroidx/compose/ui/Modifier;Landroidx/compose/runtime/Composer;II)V", "app_debug", "bounceOffset", "Landroidx/compose/ui/unit/Dp;", "localSnippets", "animateScale", "scaleFactor", "selectedColor", "selectedStyle", "selectedIndex", "isDarkSelected"}
)
@SourceDebugExtension({"SMAP\nDetailComponents.kt\nKotlin\n*S Kotlin\n*F\n+ 1 DetailComponents.kt\ncom/android/snippets/ui/DetailComponentsKt\n+ 2 Composer.kt\nandroidx/compose/runtime/ComposerKt\n+ 3 CompositionLocal.kt\nandroidx/compose/runtime/CompositionLocal\n+ 4 Dp.kt\nandroidx/compose/ui/unit/DpKt\n+ 5 Column.kt\nandroidx/compose/foundation/layout/ColumnKt\n+ 6 Layout.kt\nandroidx/compose/ui/layout/LayoutKt\n+ 7 Composables.kt\nandroidx/compose/runtime/ComposablesKt\n+ 8 fake.kt\nkotlin/jvm/internal/FakeKt\n+ 9 Box.kt\nandroidx/compose/foundation/layout/BoxKt\n+ 10 Size.kt\nandroidx/compose/ui/geometry/Size\n+ 11 InlineClassHelper.kt\nandroidx/compose/ui/util/InlineClassHelperKt\n+ 12 InlineClassHelper.jvmAndAndroid.kt\nandroidx/compose/ui/util/InlineClassHelper_jvmKt\n+ 13 Row.kt\nandroidx/compose/foundation/layout/RowKt\n+ 14 SnapshotState.kt\nandroidx/compose/runtime/SnapshotStateKt__SnapshotStateKt\n+ 15 _Collections.kt\nkotlin/collections/CollectionsKt___CollectionsKt\n+ 16 SnapshotIntState.kt\nandroidx/compose/runtime/SnapshotIntStateKt__SnapshotIntStateKt\n+ 17 LazyGridDsl.kt\nandroidx/compose/foundation/lazy/grid/LazyGridDslKt\n+ 18 _Arrays.kt\nkotlin/collections/ArraysKt___ArraysKt\n*L\n1#1,1089:1\n1047#2,6:1090\n1047#2,6:1096\n1047#2,6:1102\n1047#2,6:1108\n1047#2,6:1118\n1047#2,6:1161\n1047#2,6:1168\n1047#2,6:1213\n1047#2,6:1219\n1047#2,6:1226\n1047#2,6:1263\n1047#2,6:1269\n1047#2,6:1278\n1047#2,6:1284\n1047#2,6:1290\n1047#2,6:1302\n1047#2,6:1308\n1047#2,6:1314\n1047#2,6:1320\n1047#2,6:1326\n1047#2,6:1333\n1047#2,6:1380\n1047#2,6:1386\n1047#2,6:1392\n1047#2,6:1435\n1047#2,6:1441\n1047#2,6:1447\n1047#2,6:1490\n1047#2,6:1542\n1047#2,6:1548\n1047#2,6:1621\n1047#2,6:1717\n1047#2,6:1874\n1047#2,6:1900\n1047#2,6:1948\n1047#2,6:1954\n1047#2,6:2087\n1047#2,6:2093\n1047#2,6:2176\n1047#2,6:2182\n1047#2,3:2188\n1050#2,3:2194\n1047#2,6:2202\n1047#2,6:2318\n1047#2,6:2357\n1047#2,6:2373\n1047#2,6:2456\n1047#2,6:2462\n1047#2,6:2473\n75#3:1114\n75#3:1211\n75#3:1225\n75#3:1301\n75#3:1332\n118#4:1115\n118#4:1116\n118#4:1117\n118#4:1156\n123#4:1206\n118#4:1212\n118#4:1275\n118#4:1276\n118#4:1277\n118#4:1296\n118#4:1339\n118#4:1340\n118#4:1500\n118#4:1501\n118#4:1535\n118#4:1536\n118#4:1537\n118#4:1554\n118#4:1616\n128#4:1632\n128#4:1633\n128#4:1634\n128#4:1635\n128#4:1636\n118#4:1670\n118#4:1682\n118#4:1683\n118#4:1716\n118#4:1723\n118#4:1729\n118#4:1763\n118#4:1792\n118#4:1797\n118#4:1830\n118#4:1866\n118#4:1867\n118#4:1868\n118#4:1873\n118#4:1906\n118#4:1907\n118#4:1942\n118#4:1947\n118#4:1991\n118#4:1992\n118#4:2053\n118#4:2086\n118#4:2130\n118#4:2131\n118#4:2136\n118#4:2175\n118#4:2197\n118#4:2198\n118#4:2199\n118#4:2200\n118#4:2201\n118#4:2208\n118#4:2250\n118#4:2251\n118#4:2316\n118#4:2317\n118#4:2324\n118#4:2371\n118#4:2372\n118#4:2386\n118#4:2387\n118#4:2388\n118#4:2421\n118#4:2422\n118#4:2423\n118#4:2424\n118#4:2472\n118#4:2479\n118#4:2484\n118#4:2485\n118#4:2486\n118#4:2487\n118#4:2520\n118#4:2521\n118#4:2550\n118#4:2555\n118#4:2556\n118#4:2557\n118#4:2589\n87#5:1124\n84#5,9:1125\n94#5:1160\n87#5:1347\n83#5,10:1348\n94#5:1401\n87#5:1402\n83#5,10:1403\n94#5:1456\n87#5:1457\n83#5,10:1458\n94#5:1499\n87#5:1502\n83#5,10:1503\n94#5:1541\n87#5:1731\n84#5,9:1732\n87#5:1798\n84#5,9:1799\n94#5:1872\n94#5:1883\n87#5:2054\n84#5,9:2055\n94#5:2140\n87#5:2142\n83#5,10:2143\n94#5:2212\n87#5:2252\n84#5,9:2253\n87#5:2284\n84#5,9:2285\n94#5:2370\n94#5:2382\n87#5:2389\n84#5,9:2390\n94#5:2483\n87#5:2488\n84#5,9:2489\n94#5:2597\n81#6,6:1134\n88#6,6:1149\n96#6:1159\n81#6,6:1184\n88#6,6:1199\n96#6:1209\n81#6,6:1241\n88#6,6:1256\n96#6:1299\n81#6,6:1358\n88#6,6:1373\n96#6:1400\n81#6,6:1413\n88#6,6:1428\n96#6:1455\n81#6,6:1468\n88#6,6:1483\n96#6:1498\n81#6,6:1513\n88#6,6:1528\n96#6:1540\n81#6,6:1561\n88#6,6:1576\n81#6,6:1594\n88#6,6:1609\n96#6:1619\n96#6:1629\n81#6,6:1648\n88#6,6:1663\n96#6:1673\n81#6,6:1694\n88#6,6:1709\n96#6:1726\n81#6,6:1741\n88#6,6:1756\n81#6,6:1770\n88#6,6:1785\n96#6:1795\n81#6,6:1808\n88#6,6:1823\n81#6,6:1840\n88#6,6:1855\n96#6:1864\n96#6:1871\n96#6:1882\n81#6,6:1920\n88#6,6:1935\n96#6:1945\n81#6,6:1969\n88#6,6:1984\n96#6:1995\n81#6,6:2026\n88#6,6:2041\n96#6:2050\n81#6,6:2064\n88#6,6:2079\n81#6,6:2108\n88#6,6:2123\n96#6:2134\n96#6:2139\n81#6,6:2153\n88#6,6:2168\n96#6:2211\n81#6,6:2224\n88#6,6:2239\n96#6:2248\n81#6,6:2262\n88#6,6:2277\n81#6,6:2294\n88#6,6:2309\n81#6,6:2335\n88#6,6:2350\n96#6:2365\n96#6:2369\n96#6:2381\n81#6,6:2399\n88#6,6:2414\n81#6,6:2434\n88#6,6:2449\n96#6:2470\n96#6:2482\n81#6,6:2498\n88#6,6:2513\n81#6,6:2528\n88#6,6:2543\n96#6:2553\n81#6,6:2567\n88#6,6:2582\n96#6:2592\n96#6:2596\n402#7,9:1140\n411#7:1155\n412#7,2:1157\n402#7,9:1190\n411#7:1205\n412#7,2:1207\n402#7,9:1247\n411#7:1262\n412#7,2:1297\n402#7,9:1364\n411#7:1379\n412#7,2:1398\n402#7,9:1419\n411#7:1434\n412#7,2:1453\n402#7,9:1474\n411#7:1489\n412#7,2:1496\n402#7,9:1519\n411#7:1534\n412#7,2:1538\n402#7,9:1567\n411#7:1582\n402#7,9:1600\n411#7:1615\n412#7,2:1617\n412#7,2:1627\n402#7,9:1654\n411#7:1669\n412#7,2:1671\n402#7,9:1700\n411#7:1715\n412#7,2:1724\n402#7,9:1747\n411#7:1762\n402#7,9:1776\n411#7:1791\n412#7,2:1793\n402#7,9:1814\n411#7:1829\n402#7,9:1846\n411#7,3:1861\n412#7,2:1869\n412#7,2:1880\n402#7,9:1926\n411#7:1941\n412#7,2:1943\n402#7,9:1975\n411#7:1990\n412#7,2:1993\n402#7,9:2032\n411#7,3:2047\n402#7,9:2070\n411#7:2085\n402#7,9:2114\n411#7:2129\n412#7,2:2132\n412#7,2:2137\n402#7,9:2159\n411#7:2174\n412#7,2:2209\n402#7,9:2230\n411#7,3:2245\n402#7,9:2268\n411#7:2283\n402#7,9:2300\n411#7:2315\n402#7,9:2341\n411#7:2356\n412#7,2:2363\n412#7,2:2367\n412#7,2:2379\n402#7,9:2405\n411#7:2420\n402#7,9:2440\n411#7:2455\n412#7,2:2468\n412#7,2:2480\n402#7,9:2504\n411#7:2519\n402#7,9:2534\n411#7:2549\n412#7,2:2551\n402#7,9:2573\n411#7:2588\n412#7,2:2590\n412#7,2:2594\n1#8:1167\n70#9:1174\n67#9,9:1175\n77#9:1210\n70#9:1232\n68#9,8:1233\n77#9:1300\n70#9:1637\n66#9,10:1638\n77#9:1674\n70#9:1831\n68#9,8:1832\n77#9:1865\n70#9:1909\n66#9,10:1910\n77#9:1946\n70#9:1960\n68#9,8:1961\n77#9:1996\n70#9:2015\n66#9,10:2016\n77#9:2051\n70#9:2099\n68#9,8:2100\n77#9:2135\n70#9:2325\n67#9,9:2326\n77#9:2366\n70#9:2558\n68#9,8:2559\n77#9:2593\n57#10:1341\n61#10:1344\n60#11:1342\n70#11:1345\n23#12:1343\n23#12:1346\n99#13,6:1555\n99#13:1583\n95#13,10:1584\n106#13:1620\n106#13:1630\n99#13:1684\n96#13,9:1685\n106#13:1727\n99#13,6:1764\n106#13:1796\n99#13:2213\n95#13,10:2214\n106#13:2249\n99#13:2425\n97#13,8:2426\n106#13:2471\n99#13,6:2522\n106#13:2554\n85#14:1631\n85#14:1675\n117#14,2:1676\n85#14:1678\n117#14,2:1679\n85#14:1681\n85#14:1884\n117#14,2:1885\n85#14:1887\n117#14,2:1888\n85#14:1890\n117#14,2:1891\n85#14:2383\n117#14,2:2384\n1869#15:1728\n1870#15:1730\n1878#15,3:1896\n1869#15:1899\n1870#15:1908\n774#15:2191\n865#15,2:2192\n78#16:1893\n111#16,2:1894\n524#17,18:1997\n13805#18:2052\n13806#18:2141\n*S KotlinDebug\n*F\n+ 1 DetailComponents.kt\ncom/android/snippets/ui/DetailComponentsKt\n*L\n91#1:1090,6\n99#1:1096,6\n101#1:1102,6\n103#1:1108,6\n258#1:1118,6\n328#1:1161,6\n374#1:1168,6\n460#1:1213,6\n478#1:1219,6\n484#1:1226,6\n498#1:1263,6\n499#1:1269,6\n515#1:1278,6\n520#1:1284,6\n522#1:1290,6\n617#1:1302,6\n618#1:1308,6\n619#1:1314,6\n621#1:1320,6\n624#1:1326,6\n934#1:1333,6\n171#1:1380,6\n177#1:1386,6\n183#1:1392,6\n201#1:1435,6\n207#1:1441,6\n213#1:1447,6\n231#1:1490,6\n122#1:1542,6\n123#1:1548,6\n150#1:1621,6\n577#1:1717,6\n595#1:1874,6\n755#1:1900,6\n788#1:1948,6\n790#1:1954,6\n856#1:2087,6\n858#1:2093,6\n728#1:2176,6\n705#1:2182,6\n735#1:2188,3\n735#1:2194,3\n780#1:2202,6\n673#1:2318,6\n693#1:2357,6\n900#1:2373,6\n976#1:2456,6\n988#1:2462,6\n1003#1:2473,6\n106#1:1114\n455#1:1211\n479#1:1225\n616#1:1301\n933#1:1332\n111#1:1115\n246#1:1116\n247#1:1117\n264#1:1156\n394#1:1206\n457#1:1212\n509#1:1275\n513#1:1276\n514#1:1277\n523#1:1296\n1038#1:1339\n1040#1:1340\n155#1:1500\n157#1:1501\n163#1:1535\n193#1:1536\n224#1:1537\n128#1:1554\n143#1:1616\n404#1:1632\n410#1:1633\n418#1:1634\n426#1:1635\n434#1:1636\n465#1:1670\n582#1:1682\n574#1:1683\n576#1:1716\n581#1:1723\n573#1:1729\n527#1:1763\n534#1:1792\n542#1:1797\n544#1:1830\n550#1:1866\n551#1:1867\n552#1:1868\n599#1:1873\n765#1:1906\n767#1:1907\n805#1:1942\n786#1:1947\n796#1:1991\n799#1:1992\n849#1:2053\n854#1:2086\n864#1:2130\n867#1:2131\n882#1:2136\n711#1:2175\n748#1:2197\n750#1:2198\n776#1:2199\n777#1:2200\n778#1:2201\n842#1:2208\n650#1:2250\n651#1:2251\n666#1:2316\n670#1:2317\n688#1:2324\n894#1:2371\n906#1:2372\n1014#1:2386\n1015#1:2387\n944#1:2388\n952#1:2421\n955#1:2422\n963#1:2423\n969#1:2424\n996#1:2472\n1007#1:2479\n938#1:2484\n940#1:2485\n941#1:2486\n1043#1:2487\n1047#1:2520\n1048#1:2521\n1052#1:2550\n1068#1:2555\n1071#1:2556\n1073#1:2557\n1081#1:2589\n256#1:1124\n256#1:1125,9\n256#1:1160\n166#1:1347\n166#1:1348,10\n166#1:1401\n196#1:1402\n196#1:1403,10\n196#1:1456\n227#1:1457\n227#1:1458,10\n227#1:1499\n157#1:1502\n157#1:1503,10\n157#1:1541\n525#1:1731\n525#1:1732,9\n542#1:1798\n542#1:1799,9\n542#1:1872\n525#1:1883\n847#1:2054\n847#1:2055,9\n847#1:2140\n700#1:2142\n700#1:2143,10\n700#1:2212\n647#1:2252\n647#1:2253,9\n654#1:2284\n654#1:2285,9\n654#1:2370\n647#1:2382\n943#1:2389\n943#1:2390,9\n943#1:2483\n1042#1:2488\n1042#1:2489,9\n1042#1:2597\n256#1:1134,6\n256#1:1149,6\n256#1:1159\n390#1:1184,6\n390#1:1199,6\n390#1:1209\n481#1:1241,6\n481#1:1256,6\n481#1:1299\n166#1:1358,6\n166#1:1373,6\n166#1:1400\n196#1:1413,6\n196#1:1428,6\n196#1:1455\n227#1:1468,6\n227#1:1483,6\n227#1:1498\n157#1:1513,6\n157#1:1528,6\n157#1:1540\n113#1:1561,6\n113#1:1576,6\n132#1:1594,6\n132#1:1609,6\n132#1:1619\n113#1:1629\n465#1:1648,6\n465#1:1663,6\n465#1:1673\n574#1:1694,6\n574#1:1709,6\n574#1:1726\n525#1:1741,6\n525#1:1756,6\n527#1:1770,6\n527#1:1785,6\n527#1:1795\n542#1:1808,6\n542#1:1823,6\n544#1:1840,6\n544#1:1855,6\n544#1:1864\n542#1:1871\n525#1:1882\n801#1:1920,6\n801#1:1935,6\n801#1:1945\n783#1:1969,6\n783#1:1984,6\n783#1:1995\n869#1:2026,6\n869#1:2041,6\n869#1:2050\n847#1:2064,6\n847#1:2079,6\n851#1:2108,6\n851#1:2123,6\n851#1:2134\n847#1:2139\n700#1:2153,6\n700#1:2168,6\n700#1:2211\n908#1:2224,6\n908#1:2239,6\n908#1:2248\n647#1:2262,6\n647#1:2277,6\n654#1:2294,6\n654#1:2309,6\n687#1:2335,6\n687#1:2350,6\n687#1:2365\n654#1:2369\n647#1:2381\n943#1:2399,6\n943#1:2414,6\n967#1:2434,6\n967#1:2449,6\n967#1:2470\n943#1:2482\n1042#1:2498,6\n1042#1:2513,6\n1045#1:2528,6\n1045#1:2543,6\n1045#1:2553\n1063#1:2567,6\n1063#1:2582,6\n1063#1:2592\n1042#1:2596\n256#1:1140,9\n256#1:1155\n256#1:1157,2\n390#1:1190,9\n390#1:1205\n390#1:1207,2\n481#1:1247,9\n481#1:1262\n481#1:1297,2\n166#1:1364,9\n166#1:1379\n166#1:1398,2\n196#1:1419,9\n196#1:1434\n196#1:1453,2\n227#1:1474,9\n227#1:1489\n227#1:1496,2\n157#1:1519,9\n157#1:1534\n157#1:1538,2\n113#1:1567,9\n113#1:1582\n132#1:1600,9\n132#1:1615\n132#1:1617,2\n113#1:1627,2\n465#1:1654,9\n465#1:1669\n465#1:1671,2\n574#1:1700,9\n574#1:1715\n574#1:1724,2\n525#1:1747,9\n525#1:1762\n527#1:1776,9\n527#1:1791\n527#1:1793,2\n542#1:1814,9\n542#1:1829\n544#1:1846,9\n544#1:1861,3\n542#1:1869,2\n525#1:1880,2\n801#1:1926,9\n801#1:1941\n801#1:1943,2\n783#1:1975,9\n783#1:1990\n783#1:1993,2\n869#1:2032,9\n869#1:2047,3\n847#1:2070,9\n847#1:2085\n851#1:2114,9\n851#1:2129\n851#1:2132,2\n847#1:2137,2\n700#1:2159,9\n700#1:2174\n700#1:2209,2\n908#1:2230,9\n908#1:2245,3\n647#1:2268,9\n647#1:2283\n654#1:2300,9\n654#1:2315\n687#1:2341,9\n687#1:2356\n687#1:2363,2\n654#1:2367,2\n647#1:2379,2\n943#1:2405,9\n943#1:2420\n967#1:2440,9\n967#1:2455\n967#1:2468,2\n943#1:2480,2\n1042#1:2504,9\n1042#1:2519\n1045#1:2534,9\n1045#1:2549\n1045#1:2551,2\n1063#1:2573,9\n1063#1:2588\n1063#1:2590,2\n1042#1:2594,2\n390#1:1174\n390#1:1175,9\n390#1:1210\n481#1:1232\n481#1:1233,8\n481#1:1300\n465#1:1637\n465#1:1638,10\n465#1:1674\n544#1:1831\n544#1:1832,8\n544#1:1865\n801#1:1909\n801#1:1910,10\n801#1:1946\n783#1:1960\n783#1:1961,8\n783#1:1996\n869#1:2015\n869#1:2016,10\n869#1:2051\n851#1:2099\n851#1:2100,8\n851#1:2135\n687#1:2325\n687#1:2326,9\n687#1:2366\n1063#1:2558\n1063#1:2559,8\n1063#1:2593\n68#1:1341\n69#1:1344\n68#1:1342\n69#1:1345\n68#1:1343\n69#1:1346\n113#1:1555,6\n132#1:1583\n132#1:1584,10\n132#1:1620\n113#1:1630\n574#1:1684\n574#1:1685,9\n574#1:1727\n527#1:1764,6\n527#1:1796\n908#1:2213\n908#1:2214,10\n908#1:2249\n967#1:2425\n967#1:2426,8\n967#1:2471\n1045#1:2522,6\n1045#1:2554\n245#1:1631\n478#1:1675\n478#1:1676,2\n498#1:1678\n498#1:1679,2\n502#1:1681\n617#1:1884\n617#1:1885,2\n618#1:1887\n618#1:1888,2\n619#1:1890\n619#1:1891,2\n934#1:2383\n934#1:2384,2\n554#1:1728\n554#1:1730\n674#1:1896,3\n753#1:1899\n753#1:1908\n739#1:2191\n739#1:2192,2\n621#1:1893\n621#1:1894,2\n812#1:1997,18\n845#1:2052\n845#1:2141\n*E\n"})
public final class DetailComponentsKt {
   @NotNull
   private static final GenericShape CookieShape = new GenericShape(DetailComponentsKt::CookieShape$lambda$0);

   @NotNull
   public static final GenericShape getCookieShape() {
      return CookieShape;
   }

   @Composable
   @ComposableTarget(
      applier = "androidx.compose.ui.UiComposable"
   )
   public static final void DetailTopBar(@NotNull Photo photo, @NotNull SnippetsViewModel viewModel, @NotNull Function0<Unit> onBack, boolean isSpinning, boolean isScrolled, @Nullable Function0<Unit> onPhotoThumbnailClick, boolean hasSnippets, @NotNull Function0<Unit> onAdd, @NotNull Function0<Unit> onDownload, @NotNull Function0<Unit> onEdit, @NotNull Function0<Unit> onShare, @NotNull Function0<Unit> onDelete, boolean isFavorite, @Nullable Function0<Unit> onToggleFavorite, boolean isPublic, @Nullable Function0<Unit> onTogglePublic, boolean hasLocationLink, @Nullable Function0<Unit> onAddLinkClick, @Nullable AnimatedVisibilityScope animatedVisibilityScope, @Nullable Composer $composer, int $changed, int $changed1, int var22) {
      Intrinsics.checkNotNullParameter(photo, "photo");
      Intrinsics.checkNotNullParameter(viewModel, "viewModel");
      Intrinsics.checkNotNullParameter(onBack, "onBack");
      Intrinsics.checkNotNullParameter(onAdd, "onAdd");
      Intrinsics.checkNotNullParameter(onDownload, "onDownload");
      Intrinsics.checkNotNullParameter(onEdit, "onEdit");
      Intrinsics.checkNotNullParameter(onShare, "onShare");
      Intrinsics.checkNotNullParameter(onDelete, "onDelete");
      $composer = $composer.startRestartGroup(1722028461);
      ComposerKt.sourceInformation($composer, "C(DetailTopBar)N(photo,viewModel,onBack,isSpinning,isScrolled,onPhotoThumbnailClick,hasSnippets,onAdd,onDownload,onEdit,onShare,onDelete,isFavorite,onToggleFavorite,isPublic,onTogglePublic,hasLocationLink,onAddLinkClick,animatedVisibilityScope)90@3809L2,98@4033L2,100@4101L2,102@4176L2,105@4297L7,111@4465L7336,106@4309L7492:DetailComponents.kt#wxf82o");
      int $dirty = $changed;
      int $dirty1 = $changed1;
      if (($changed & 384) == 0) {
         $dirty = $changed | ($composer.changedInstance(onBack) ? 256 : 128);
      }

      if ((var22 & 8) != 0) {
         $dirty |= 3072;
      } else if (($changed & 3072) == 0) {
         $dirty |= $composer.changed(isSpinning) ? 2048 : 1024;
      }

      if (($changed & 1572864) == 0) {
         $dirty |= $composer.changed(hasSnippets) ? 1048576 : 524288;
      }

      if (($changed & 12582912) == 0) {
         $dirty |= $composer.changedInstance(onAdd) ? 8388608 : 4194304;
      }

      if (($changed & 100663296) == 0) {
         $dirty |= $composer.changedInstance(onDownload) ? 67108864 : 33554432;
      }

      if (($changed & 805306368) == 0) {
         $dirty |= $composer.changedInstance(onEdit) ? 536870912 : 268435456;
      }

      if (($changed1 & 6) == 0) {
         $dirty1 = $changed1 | ($composer.changedInstance(onShare) ? 4 : 2);
      }

      if (($changed1 & 48) == 0) {
         $dirty1 |= $composer.changedInstance(onDelete) ? 32 : 16;
      }

      if ((var22 & 4096) != 0) {
         $dirty1 |= 384;
      } else if (($changed1 & 384) == 0) {
         $dirty1 |= $composer.changed(isFavorite) ? 256 : 128;
      }

      if ((var22 & 8192) != 0) {
         $dirty1 |= 3072;
      } else if (($changed1 & 3072) == 0) {
         $dirty1 |= $composer.changedInstance(onToggleFavorite) ? 2048 : 1024;
      }

      if ((var22 & 16384) != 0) {
         $dirty1 |= 24576;
      } else if (($changed1 & 24576) == 0) {
         $dirty1 |= $composer.changed(isPublic) ? 16384 : 8192;
      }

      if ((var22 & '耀') != 0) {
         $dirty1 |= 196608;
      } else if (($changed1 & 196608) == 0) {
         $dirty1 |= $composer.changedInstance(onTogglePublic) ? 131072 : 65536;
      }

      if ((var22 & 65536) != 0) {
         $dirty1 |= 1572864;
      } else if (($changed1 & 1572864) == 0) {
         $dirty1 |= $composer.changed(hasLocationLink) ? 1048576 : 524288;
      }

      if ((var22 & 131072) != 0) {
         $dirty1 |= 12582912;
      } else if (($changed1 & 12582912) == 0) {
         $dirty1 |= $composer.changedInstance(onAddLinkClick) ? 8388608 : 4194304;
      }

      if ((var22 & 262144) != 0) {
         $dirty1 |= 100663296;
      } else if (($changed1 & 100663296) == 0) {
         $dirty1 |= $composer.changedInstance(animatedVisibilityScope) ? 67108864 : 33554432;
      }

      if ($composer.shouldExecute(($dirty & 306709633) != 306709632 || ($dirty1 & 38347923) != 38347922, $dirty & 1)) {
         if ((var22 & 8) != 0) {
            isSpinning = true;
         }

         if ((var22 & 16) != 0) {
            isScrolled = false;
         }

         if ((var22 & 32) != 0) {
            ComposerKt.sourceInformationMarkerStart($composer, 1306949679, "CC(remember):DetailComponents.kt#9igjgp");
            boolean var27 = false;
            int var28 = 0;
            Object var29 = $composer.rememberedValue();
            int var30 = 0;
            Object var10000;
            if (var29 == Composer.Companion.getEmpty()) {
               int var31 = 0;
               Object var57 = DetailComponentsKt::DetailTopBar$lambda$2$lambda$1;
               $composer.updateRememberedValue(var57);
               var10000 = var57;
            } else {
               var10000 = var29;
            }

            Function0 var25 = (Function0)var10000;
            ComposerKt.sourceInformationMarkerEnd($composer);
            onPhotoThumbnailClick = var25;
         }

         if ((var22 & 4096) != 0) {
            isFavorite = false;
         }

         if ((var22 & 8192) != 0) {
            ComposerKt.sourceInformationMarkerStart($composer, 1306956847, "CC(remember):DetailComponents.kt#9igjgp");
            boolean var39 = false;
            int var43 = 0;
            Object var47 = $composer.rememberedValue();
            int var52 = 0;
            Object var65;
            if (var47 == Composer.Companion.getEmpty()) {
               int var58 = 0;
               Object var59 = DetailComponentsKt::DetailTopBar$lambda$4$lambda$3;
               $composer.updateRememberedValue(var59);
               var65 = var59;
            } else {
               var65 = var47;
            }

            Function0 var34 = (Function0)var65;
            ComposerKt.sourceInformationMarkerEnd($composer);
            onToggleFavorite = var34;
         }

         if ((var22 & 16384) != 0) {
            isPublic = false;
         }

         if ((var22 & '耀') != 0) {
            ComposerKt.sourceInformationMarkerStart($composer, 1306959023, "CC(remember):DetailComponents.kt#9igjgp");
            boolean var40 = false;
            int var44 = 0;
            Object var48 = $composer.rememberedValue();
            int var53 = 0;
            Object var66;
            if (var48 == Composer.Companion.getEmpty()) {
               int var60 = 0;
               Object var61 = DetailComponentsKt::DetailTopBar$lambda$6$lambda$5;
               $composer.updateRememberedValue(var61);
               var66 = var61;
            } else {
               var66 = var48;
            }

            Function0 var35 = (Function0)var66;
            ComposerKt.sourceInformationMarkerEnd($composer);
            onTogglePublic = var35;
         }

         if ((var22 & 65536) != 0) {
            hasLocationLink = false;
         }

         if ((var22 & 131072) != 0) {
            ComposerKt.sourceInformationMarkerStart($composer, 1306961423, "CC(remember):DetailComponents.kt#9igjgp");
            boolean var41 = false;
            int var45 = 0;
            Object var49 = $composer.rememberedValue();
            int var54 = 0;
            Object var67;
            if (var49 == Composer.Companion.getEmpty()) {
               int var62 = 0;
               Object var63 = DetailComponentsKt::DetailTopBar$lambda$8$lambda$7;
               $composer.updateRememberedValue(var63);
               var67 = var63;
            } else {
               var67 = var49;
            }

            Function0 var36 = (Function0)var67;
            ComposerKt.sourceInformationMarkerEnd($composer);
            onAddLinkClick = var36;
         }

         if ((var22 & 262144) != 0) {
            animatedVisibilityScope = null;
         }

         if (ComposerKt.isTraceInProgress()) {
            ComposerKt.traceEventStart(1722028461, $dirty, $dirty1, "com.android.snippets.ui.DetailTopBar (DetailComponents.kt:104)");
         }

         CompositionLocal var26 = (CompositionLocal)AndroidCompositionLocals_androidKt.getLocalView();
         int var46 = 0;
         int var50 = 0;
         ComposerKt.sourceInformationMarkerStart($composer, 2023513938, "CC(<get-current>):CompositionLocal.kt#9igjgp");
         Object var55 = $composer.consume(var26);
         ComposerKt.sourceInformationMarkerEnd($composer);
         View view = (View)var55;
         Modifier var38 = SizeKt.fillMaxWidth$default((Modifier)Modifier.Companion, 0.0F, 1, (Object)null);
         long var42 = Color.Companion.getTransparent-0d7_KjU();
         Shape var51 = RectangleShapeKt.getRectangleShape();
         int var64 = 0;
         int var32 = 0;
         float var56 = Dp.constructor-impl((float)var64);
         SurfaceKt.Surface-T9BRK9s(var38, var51, var42, 0L, var56, 0.0F, (BorderStroke)null, (Function2)ComposableLambdaKt.rememberComposableLambda(1897722738, true, DetailComponentsKt::DetailTopBar$lambda$45, $composer, 54), $composer, 12607926, 104);
         if (ComposerKt.isTraceInProgress()) {
            ComposerKt.traceEventEnd();
         }
      } else {
         $composer.skipToGroupEnd();
      }

      ScopeUpdateScope var68 = $composer.endRestartGroup();
      if (var68 != null) {
         var68.updateScope(DetailComponentsKt::DetailTopBar$lambda$46);
      }

   }

   @Composable
   @ComposableTarget(
      applier = "androidx.compose.ui.UiComposable"
   )
   public static final void SwipePrompt(@Nullable Modifier modifier, float alphaValue, @Nullable Composer $composer, int $changed, int var4) {
      $composer = $composer.startRestartGroup(1658619472);
      ComposerKt.sourceInformation($composer, "C(SwipePrompt)N(modifier,alphaValue)243@11921L44,244@12009L339,257@12464L39,255@12354L660:DetailComponents.kt#wxf82o");
      int $dirty = $changed;
      if ((var4 & 1) != 0) {
         $dirty = $changed | 6;
      } else if (($changed & 6) == 0) {
         $dirty = $changed | ($composer.changed(modifier) ? 4 : 2);
      }

      if ((var4 & 2) != 0) {
         $dirty |= 48;
      } else if (($changed & 48) == 0) {
         $dirty |= $composer.changed(alphaValue) ? 32 : 16;
      }

      if ($composer.shouldExecute(($dirty & 19) != 18, $dirty & 1)) {
         if ((var4 & 1) != 0) {
            modifier = (Modifier)Modifier.Companion;
         }

         if ((var4 & 2) != 0) {
            alphaValue = 0.6F;
         }

         if (ComposerKt.isTraceInProgress()) {
            ComposerKt.traceEventStart(1658619472, $dirty, -1, "com.android.snippets.ui.SwipePrompt (DetailComponents.kt:242)");
         }

         InfiniteTransition infiniteTransition = InfiniteTransitionKt.rememberInfiniteTransition("bounce", $composer, 6, 0);
         int var8 = 0;
         int var9 = 0;
         Dp var10001 = Dp.box-impl(Dp.constructor-impl((float)var8));
         var8 = 12;
         var9 = 0;
         State bounceOffset$delegate = InfiniteTransitionKt.animateValue(infiniteTransition, var10001, Dp.box-impl(Dp.constructor-impl((float)var8)), VectorConvertersKt.getVectorConverter(Dp.Companion), AnimationSpecKt.infiniteRepeatable-9IiC70o$default((DurationBasedAnimationSpec)AnimationSpecKt.tween$default(1000, 0, EasingKt.getLinearOutSlowInEasing(), 2, (Object)null), RepeatMode.Reverse, 0L, 4, (Object)null), "bounce", $composer, 197040 | InfiniteTransition.$stable | InfiniteRepeatableSpec.$stable << 12, 0);
         Alignment.Horizontal var52 = Alignment.Companion.getCenterHorizontally();
         Modifier var10000 = modifier;
         ComposerKt.sourceInformationMarkerStart($composer, 500466903, "CC(remember):DetailComponents.kt#9igjgp");
         boolean var12 = ($dirty & 112) == 32;
         int var13 = 0;
         Object var14 = $composer.rememberedValue();
         int var15 = 0;
         Object var63;
         if (!var12 && var14 != Composer.Companion.getEmpty()) {
            var63 = var14;
         } else {
            int var16 = 0;
            Function1 var62 = DetailComponentsKt::SwipePrompt$lambda$49$lambda$48;
            var10000 = modifier;
            Object var17 = var62;
            $composer.updateRememberedValue(var17);
            var63 = var17;
         }

         Function1 var10 = (Function1)var63;
         ComposerKt.sourceInformationMarkerEnd($composer);
         Modifier var54 = GraphicsLayerModifierKt.graphicsLayer(var10000, var10);
         short var55 = 384;
         var15 = 0;
         ComposerKt.sourceInformationMarkerStart($composer, 1341605231, "CC(Column)N(modifier,verticalArrangement,horizontalAlignment,content)87@4443L61,88@4509L134:Column.kt#2w3rfo");
         Arrangement.Vertical var11 = Arrangement.INSTANCE.getTop();
         MeasurePolicy var57 = ColumnKt.columnMeasurePolicy(var11, var52, $composer, 14 & var55 >> 3 | 112 & var55 >> 3);
         int var20 = 112 & var55 << 3;
         int var21 = 0;
         ComposerKt.sourceInformationMarkerStart($composer, -1159599143, "CC(Layout)N(content,modifier,measurePolicy)81@3355L27,84@3521L415:Layout.kt#80mrfh");
         int var22 = Long.hashCode(ComposablesKt.getCurrentCompositeKeyHashCode($composer, 0));
         CompositionLocalMap var23 = $composer.getCurrentCompositionLocalMap();
         Modifier var24 = ComposedModifierKt.materializeModifier($composer, var54);
         Function0 var25 = ComposeUiNode.Companion.getConstructor();
         int var27 = 6 | 896 & var20 << 6;
         int var28 = 0;
         ComposerKt.sourceInformationMarkerStart($composer, -553112988, "CC(ReusableComposeNode)N(factory,update,content)410@16187L9:Composables.kt#9igjgp");
         if (!($composer.getApplier() instanceof Applier)) {
            ComposablesKt.invalidApplier();
         }

         $composer.startReusableNode();
         if ($composer.getInserting()) {
            $composer.createNode(var25);
         } else {
            $composer.useNode();
         }

         Composer var29 = Updater.constructor-impl($composer);
         int var30 = 0;
         Updater.set-impl(var29, var57, ComposeUiNode.Companion.getSetMeasurePolicy());
         Updater.set-impl(var29, var23, ComposeUiNode.Companion.getSetResolvedCompositionLocals());
         Updater.set-impl(var29, var22, ComposeUiNode.Companion.getSetCompositeKeyHash());
         Updater.reconcile-impl(var29, ComposeUiNode.Companion.getApplyOnDeactivatedNodeAssertion());
         Updater.set-impl(var29, var24, ComposeUiNode.Companion.getSetModifier());
         int var31 = 14 & var27 >> 6;
         int var33 = 0;
         ComposerKt.sourceInformationMarkerStart($composer, 2093002350, "C89@4557L9:Column.kt#2w3rfo");
         int var34 = 6 | 112 & var55 >> 6;
         ColumnScope var59 = (ColumnScope)ColumnScopeInstance.INSTANCE;
         int var37 = 0;
         ComposerKt.sourceInformationMarkerStart($composer, -563905151, "C262@12650L11,259@12520L231,267@12844L10,270@12977L11,265@12760L248:DetailComponents.kt#wxf82o");
         ImageVector var38 = ExpandMoreKt.getExpandMore(Icons.INSTANCE.getDefault());
         long var39 = MaterialTheme.INSTANCE.getColorScheme($composer, MaterialTheme.$stable).getOnSurface-0d7_KjU();
         Modifier var60 = (Modifier)Modifier.Companion;
         int var41 = 36;
         int var42 = 0;
         Modifier var43 = OffsetKt.offset-VpY3zN4$default(SizeKt.size-3ABfNKs(var60, Dp.constructor-impl((float)var41)), 0.0F, SwipePrompt$lambda$47(bounceOffset$delegate), 1, (Object)null);
         IconKt.Icon-ww6aTOc(var38, (String)null, var43, var39, $composer, 48, 0);
         TextStyle var58 = MaterialTheme.INSTANCE.getTypography($composer, MaterialTheme.$stable).getLabelSmall();
         FontWeight var44 = FontWeight.Companion.getBold();
         long var45 = TextUnitKt.getSp(2);
         long var47 = MaterialTheme.INSTANCE.getColorScheme($composer, MaterialTheme.$stable).getOnSurface-0d7_KjU();
         TextKt.Text-Nvy7gAk("SWIPE TO SEE SNIPPETS", (Modifier)null, var47, (TextAutoSize)null, 0L, (FontStyle)null, var44, (FontFamily)null, var45, (TextDecoration)null, (TextAlign)null, 0L, 0, false, 0, 0, (Function1)null, var58, $composer, 102236166, 0, 130746);
         ComposerKt.sourceInformationMarkerEnd($composer);
         ComposerKt.sourceInformationMarkerEnd($composer);
         $composer.endNode();
         ComposerKt.sourceInformationMarkerEnd($composer);
         ComposerKt.sourceInformationMarkerEnd($composer);
         ComposerKt.sourceInformationMarkerEnd($composer);
         if (ComposerKt.isTraceInProgress()) {
            ComposerKt.traceEventEnd();
         }
      } else {
         $composer.skipToGroupEnd();
      }

      ScopeUpdateScope var61 = $composer.endRestartGroup();
      if (var61 != null) {
         var61.updateScope(DetailComponentsKt::SwipePrompt$lambda$51);
      }

   }

   @Composable
   @NotNull
   public static final TextStyle getSnippetTextStyle(@NotNull SnippetStyle style, @NotNull TextStyle base, boolean isCloud, @Nullable Composer $composer, int $changed, int var5) {
      Intrinsics.checkNotNullParameter(style, "style");
      Intrinsics.checkNotNullParameter(base, "base");
      ComposerKt.sourceInformationMarkerStart($composer, -1810362110, "C(getSnippetTextStyle)N(style,base,isCloud):DetailComponents.kt#wxf82o");
      if ((var5 & 4) != 0) {
         isCloud = false;
      }

      if (ComposerKt.isTraceInProgress()) {
         ComposerKt.traceEventStart(-1810362110, $changed, -1, "com.android.snippets.ui.getSnippetTextStyle (DetailComponents.kt:280)");
      }

      TextStyle var15;
      switch (com.android.snippets.ui.DetailComponentsKt.WhenMappings.$EnumSwitchMapping$0[style.ordinal()]) {
         case 1:
            var15 = TextStyle.copy-p1EtxEg$default(base, 0L, 0L, FontWeight.Companion.getThin(), (FontStyle)null, (FontSynthesis)null, (FontFamily)FontFamily.Companion.getSansSerif(), (String)null, 0L, (BaselineShift)null, (TextGeometricTransform)null, (LocaleList)null, 0L, (TextDecoration)null, (Shadow)null, (DrawStyle)null, 0, 0, 0L, (TextIndent)null, (PlatformTextStyle)null, (LineHeightStyle)null, 0, 0, (TextMotion)null, 16777179, (Object)null);
            break;
         case 2:
            var15 = TextStyle.copy-p1EtxEg$default(base, 0L, 0L, (FontWeight)null, (FontStyle)null, (FontSynthesis)null, (FontFamily)FontFamily.Companion.getCursive(), (String)null, 0L, (BaselineShift)null, (TextGeometricTransform)null, (LocaleList)null, 0L, (TextDecoration)null, (Shadow)null, (DrawStyle)null, 0, 0, 0L, (TextIndent)null, (PlatformTextStyle)null, (LineHeightStyle)null, 0, 0, (TextMotion)null, 16777183, (Object)null);
            break;
         case 3:
            var15 = TextStyle.copy-p1EtxEg$default(base, 0L, 0L, (FontWeight)null, (FontStyle)null, (FontSynthesis)null, (FontFamily)FontFamily.Companion.getMonospace(), (String)null, 0L, (BaselineShift)null, (TextGeometricTransform)null, (LocaleList)null, 0L, (TextDecoration)null, (Shadow)null, (DrawStyle)null, 0, 0, 0L, (TextIndent)null, (PlatformTextStyle)null, (LineHeightStyle)null, 0, 0, (TextMotion)null, 16777183, (Object)null);
            break;
         case 4:
            var15 = TextStyle.copy-p1EtxEg$default(base, 0L, 0L, (FontWeight)null, (FontStyle)null, (FontSynthesis)null, (FontFamily)FontFamily.Companion.getSerif(), (String)null, 0L, (BaselineShift)null, (TextGeometricTransform)null, (LocaleList)null, 0L, (TextDecoration)null, (Shadow)null, (DrawStyle)null, 0, 0, 0L, (TextIndent)null, (PlatformTextStyle)null, (LineHeightStyle)null, 0, 0, (TextMotion)null, 16777183, (Object)null);
            break;
         case 5:
            long var12 = isCloud ? TextUnitKt.getSp((double)1.5F) : TextUnitKt.getSp(4);
            FontWeight var9 = FontWeight.Companion.getBold();
            var15 = TextStyle.copy-p1EtxEg$default(base, 0L, 0L, var9, (FontStyle)null, (FontSynthesis)null, (FontFamily)null, (String)null, var12, (BaselineShift)null, (TextGeometricTransform)null, (LocaleList)null, 0L, (TextDecoration)null, (Shadow)null, (DrawStyle)null, 0, 0, 0L, (TextIndent)null, (PlatformTextStyle)null, (LineHeightStyle)null, 0, 0, (TextMotion)null, 16777083, (Object)null);
            break;
         case 6:
            FontWeight var10003 = FontWeight.Companion.getMedium();
            Typeface var10006 = Typeface.create("sans-serif-rounded", 0);
            Intrinsics.checkNotNullExpressionValue(var10006, "create(...)");
            var15 = TextStyle.copy-p1EtxEg$default(base, 0L, 0L, var10003, (FontStyle)null, (FontSynthesis)null, AndroidTypeface_androidKt.FontFamily(var10006), (String)null, TextUnitKt.getSp(0.8), (BaselineShift)null, (TextGeometricTransform)null, (LocaleList)null, 0L, (TextDecoration)null, (Shadow)null, (DrawStyle)null, 0, 0, 0L, (TextIndent)null, (PlatformTextStyle)null, (LineHeightStyle)null, 0, 0, (TextMotion)null, 16777051, (Object)null);
            break;
         case 7:
            FontWeight var11 = FontWeight.Companion.getBlack();
            long var13 = TextUnitKt.getSp(-1);
            Typeface var16 = Typeface.create("sans-serif-condensed", 1);
            Intrinsics.checkNotNullExpressionValue(var16, "create(...)");
            FontFamily var14 = AndroidTypeface_androidKt.FontFamily(var16);
            var15 = TextStyle.copy-p1EtxEg$default(base, 0L, 0L, var11, (FontStyle)null, (FontSynthesis)null, var14, (String)null, var13, (BaselineShift)null, (TextGeometricTransform)null, (LocaleList)null, 0L, (TextDecoration)null, (Shadow)null, (DrawStyle)null, 0, 0, 0L, (TextIndent)null, (PlatformTextStyle)null, (LineHeightStyle)null, 0, 0, (TextMotion)null, 16777051, (Object)null);
            break;
         case 8:
            FontWeight var7 = FontWeight.Companion.getBlack();
            long var8 = TextUnitKt.getSp((double)-0.5F);
            Typeface var10000 = Typeface.create("sans-serif-black", 0);
            Intrinsics.checkNotNullExpressionValue(var10000, "create(...)");
            FontFamily var10 = AndroidTypeface_androidKt.FontFamily(var10000);
            var15 = TextStyle.copy-p1EtxEg$default(base, 0L, 0L, var7, (FontStyle)null, (FontSynthesis)null, var10, (String)null, var8, (BaselineShift)null, (TextGeometricTransform)null, (LocaleList)null, 0L, (TextDecoration)null, (Shadow)null, (DrawStyle)null, 0, 0, 0L, (TextIndent)null, (PlatformTextStyle)null, (LineHeightStyle)null, 0, 0, (TextMotion)null, 16777051, (Object)null);
            break;
         default:
            var15 = base;
      }

      TextStyle var6 = var15;
      if (ComposerKt.isTraceInProgress()) {
         ComposerKt.traceEventEnd();
      }

      ComposerKt.sourceInformationMarkerEnd($composer);
      return var6;
   }

   @Composable
   @ComposableTarget(
      applier = "androidx.compose.ui.UiComposable"
   )
   public static final void CloudSnippetItem(@NotNull String text, int index, int totalCount, @Nullable List<Integer> photoColors, @Nullable Integer forcedColor, @Nullable SnippetStyle forcedStyle, @Nullable Composer $composer, int $changed, int var8) {
      Intrinsics.checkNotNullParameter(text, "text");
      $composer = $composer.startRestartGroup(620037530);
      ComposerKt.sourceInformation($composer, "C(CloudSnippetItem)N(text,index,totalCount,photoColors,forcedColor,forcedStyle)327@15382L42,333@15666L11,373@17466L627,389@18442L3348:DetailComponents.kt#wxf82o");
      int $dirty = $changed;
      if (($changed & 6) == 0) {
         $dirty = $changed | ($composer.changed(text) ? 4 : 2);
      }

      if (($changed & 48) == 0) {
         $dirty |= $composer.changed(index) ? 32 : 16;
      }

      if (($changed & 384) == 0) {
         $dirty |= $composer.changed(totalCount) ? 256 : 128;
      }

      if ((var8 & 8) != 0) {
         $dirty |= 3072;
      } else if (($changed & 3072) == 0) {
         $dirty |= $composer.changedInstance(photoColors) ? 2048 : 1024;
      }

      if ((var8 & 16) != 0) {
         $dirty |= 24576;
      } else if (($changed & 24576) == 0) {
         $dirty |= $composer.changed(forcedColor) ? 16384 : 8192;
      }

      if ((var8 & 32) != 0) {
         $dirty |= 196608;
      } else if (($changed & 196608) == 0) {
         $dirty |= $composer.changed(forcedStyle == null ? -1 : ((Enum)forcedStyle).ordinal()) ? 131072 : 65536;
      }

      if ($composer.shouldExecute(($dirty & 74899) != 74898, $dirty & 1)) {
         if ((var8 & 8) != 0) {
            photoColors = CollectionsKt.emptyList();
         }

         if ((var8 & 16) != 0) {
            forcedColor = null;
         }

         if ((var8 & 32) != 0) {
            forcedStyle = null;
         }

         if (ComposerKt.isTraceInProgress()) {
            ComposerKt.traceEventStart(620037530, $dirty, -1, "com.android.snippets.ui.CloudSnippetItem (DetailComponents.kt:326)");
         }

         ComposerKt.sourceInformationMarkerStart($composer, -1144379452, "CC(remember):DetailComponents.kt#9igjgp");
         boolean var13 = ($dirty & 14) == 4;
         int var14 = 0;
         Object var28_1 = $composer.rememberedValue();
         int var17 = 0;
         Object var10000;
         if (!var13 && var28_1 != Composer.Companion.getEmpty()) {
            var10000 = var28_1;
         } else {
            int var18 = 0;
            Object var19 = RandomKt.Random(text.hashCode());
            $composer.updateRememberedValue(var19);
            var10000 = var19;
         }

         Random var11 = (Random)var10000;
         ComposerKt.sourceInformationMarkerEnd($composer);
         Random stableRandom = var11;
         int personality = var11.nextInt(0, 5);
         int colorStrategy = (index + stableRandom.nextInt(0, 10)) % 3;
         float rotation = (float)stableRandom.nextInt(-5, 5);
         long var61 = MaterialTheme.INSTANCE.getColorScheme($composer, MaterialTheme.$stable).getSurface-0d7_KjU();
         int var64 = 0;
         var14 = !(Color.getRed-impl(var61) + Color.getGreen-impl(var61) + Color.getBlue-impl(var61) > 1.5F);
         long var78;
         if (forcedColor != null) {
            $composer.startReplaceGroup(-1115629082);
            $composer.endReplaceGroup();
            var78 = ColorKt.Color(forcedColor);
         } else {
            $composer.startReplaceGroup(-1115542468);
            ComposerKt.sourceInformation($composer, "");
            switch (colorStrategy) {
               case 0:
                  $composer.startReplaceGroup(-1115534811);
                  ComposerKt.sourceInformation($composer, "");
                  if (!((Collection)photoColors).isEmpty()) {
                     $composer.startReplaceGroup(-1144361612);
                     $composer.endReplaceGroup();
                     var78 = ColorKt.Color(((Number)photoColors.get(stableRandom.nextInt(photoColors.size()))).intValue());
                  } else {
                     $composer.startReplaceGroup(-1144358271);
                     ComposerKt.sourceInformation($composer, "340@16033L11");
                     long var70 = MaterialTheme.INSTANCE.getColorScheme($composer, MaterialTheme.$stable).getPrimary-0d7_KjU();
                     $composer.endReplaceGroup();
                     var78 = var70;
                  }

                  long var66 = var78;
                  $composer.endReplaceGroup();
                  var78 = var66;
                  break;
               case 1:
                  $composer.startReplaceGroup(-1115317346);
                  ComposerKt.sourceInformation($composer, "344@16182L11,345@16237L11,346@16294L11,347@16350L11");
                  Color[] var24 = new Color[]{Color.box-impl(MaterialTheme.INSTANCE.getColorScheme($composer, MaterialTheme.$stable).getPrimary-0d7_KjU()), Color.box-impl(MaterialTheme.INSTANCE.getColorScheme($composer, MaterialTheme.$stable).getSecondary-0d7_KjU()), Color.box-impl(MaterialTheme.INSTANCE.getColorScheme($composer, MaterialTheme.$stable).getTertiary-0d7_KjU()), Color.box-impl(MaterialTheme.INSTANCE.getColorScheme($composer, MaterialTheme.$stable).getOnSurfaceVariant-0d7_KjU())};
                  List themeColors = CollectionsKt.listOf(var24);
                  long var21 = ((Color)themeColors.get(stableRandom.nextInt(themeColors.size()))).unbox-impl();
                  $composer.endReplaceGroup();
                  var78 = var21;
                  break;
               default:
                  $composer.startReplaceGroup(-1114891406);
                  $composer.endReplaceGroup();
                  List var81;
                  if (var14) {
                     Color[] var22 = new Color[]{Color.box-impl(ColorKt.Color(4294937189L)), Color.box-impl(ColorKt.Color(4293943954L)), Color.box-impl(ColorKt.Color(4290406600L)), Color.box-impl(ColorKt.Color(4283289825L)), Color.box-impl(ColorKt.Color(4286695300L)), Color.box-impl(ColorKt.Color(4294956367L))};
                     var81 = CollectionsKt.listOf(var22);
                  } else {
                     Color[] var68 = new Color[]{Color.box-impl(ColorKt.Color(4292363029L)), Color.box-impl(ColorKt.Color(4290910299L)), Color.box-impl(ColorKt.Color(4286259106L)), Color.box-impl(ColorKt.Color(4278228903L)), Color.box-impl(ColorKt.Color(4281896508L)), Color.box-impl(ColorKt.Color(4294942720L))};
                     var81 = CollectionsKt.listOf(var68);
                  }

                  List vividColors = var81;
                  var78 = ((Color)vividColors.get(stableRandom.nextInt(vividColors.size()))).unbox-impl();
            }

            long snippetColor = var78;
            $composer.endReplaceGroup();
            var78 = snippetColor;
         }

         long baseSnippetColor = var78;
         ComposerKt.sourceInformationMarkerStart($composer, -1144312179, "CC(remember):DetailComponents.kt#9igjgp");
         boolean var71 = $composer.changed(baseSnippetColor) | $composer.changed((boolean)var14);
         int var74 = 0;
         Object var25 = $composer.rememberedValue();
         int var26 = 0;
         Object var82;
         if (!var71 && var25 != Composer.Companion.getEmpty()) {
            var82 = var25;
         } else {
            int var27 = 0;
            int var30 = 0;
            float var31 = 0.299F * Color.getRed-impl(baseSnippetColor) + 0.587F * Color.getGreen-impl(baseSnippetColor) + 0.114F * Color.getBlue-impl(baseSnippetColor);
            Object var45_1 = Color.box-impl(var14 && var31 < 0.3F ? Color.copy-wmQWz5c$default(baseSnippetColor, 0.0F, RangesKt.coerceAtMost(Color.getRed-impl(baseSnippetColor) + 0.4F, 1.0F), RangesKt.coerceAtMost(Color.getGreen-impl(baseSnippetColor) + 0.4F, 1.0F), RangesKt.coerceAtMost(Color.getBlue-impl(baseSnippetColor) + 0.4F, 1.0F), 1, (Object)null) : (!var14 && var31 > 0.7F ? Color.copy-wmQWz5c$default(baseSnippetColor, 0.0F, RangesKt.coerceAtLeast(Color.getRed-impl(baseSnippetColor) - 0.4F, 0.0F), RangesKt.coerceAtLeast(Color.getGreen-impl(baseSnippetColor) - 0.4F, 0.0F), RangesKt.coerceAtLeast(Color.getBlue-impl(baseSnippetColor) - 0.4F, 0.0F), 1, (Object)null) : baseSnippetColor));
            $composer.updateRememberedValue(var45_1);
            var82 = var45_1;
         }

         long containerColor = ((Color)var82).unbox-impl();
         ComposerKt.sourceInformationMarkerEnd($composer);
         long snippetColor = containerColor;
         long var83;
         switch (personality) {
            case 1:
               $composer.startReplaceGroup(-1144290355);
               $composer.endReplaceGroup();
               var83 = Color.copy-wmQWz5c$default(containerColor, 0.15F, 0.0F, 0.0F, 0.0F, 14, (Object)null);
               break;
            case 2:
               $composer.startReplaceGroup(-1144289115);
               $composer.endReplaceGroup();
               var83 = Color.Companion.getTransparent-0d7_KjU();
               break;
            case 3:
               $composer.startReplaceGroup(-1144286996);
               ComposerKt.sourceInformation($composer, "383@18245L11");
               long var72 = Color.copy-wmQWz5c$default(MaterialTheme.INSTANCE.getColorScheme($composer, MaterialTheme.$stable).getSurfaceVariant-0d7_KjU(), 0.3F, 0.0F, 0.0F, 0.0F, 14, (Object)null);
               $composer.endReplaceGroup();
               var83 = var72;
               break;
            default:
               $composer.startReplaceGroup(-1144285459);
               $composer.endReplaceGroup();
               var83 = Color.copy-wmQWz5c$default(containerColor, 0.08F, 0.0F, 0.0F, 0.0F, 14, (Object)null);
         }

         containerColor = var83;
         Color borderColor = personality == 2 ? Color.box-impl(Color.copy-wmQWz5c$default(snippetColor, 0.4F, 0.0F, 0.0F, 0.0F, 14, (Object)null)) : null;
         Modifier var73 = ModifierExtKt.rotateWithBounds((Modifier)Modifier.Companion, rotation);
         int var77 = 0;
         int var28 = 0;
         ComposerKt.sourceInformationMarkerStart($composer, 1042775818, "CC(Box)N(modifier,contentAlignment,propagateMinConstraints,content)71@3424L131:Box.kt#2w3rfo");
         Alignment var75 = Alignment.Companion.getTopStart();
         boolean var76 = false;
         MeasurePolicy var29 = BoxKt.maybeCachedBoxMeasurePolicy(var75, var76);
         int var33 = 112 & var77 << 3;
         int var34 = 0;
         ComposerKt.sourceInformationMarkerStart($composer, -1159599143, "CC(Layout)N(content,modifier,measurePolicy)81@3355L27,84@3521L415:Layout.kt#80mrfh");
         int var35 = Long.hashCode(ComposablesKt.getCurrentCompositeKeyHashCode($composer, 0));
         CompositionLocalMap var36 = $composer.getCurrentCompositionLocalMap();
         Modifier var37 = ComposedModifierKt.materializeModifier($composer, var73);
         Function0 var38 = ComposeUiNode.Companion.getConstructor();
         int var40 = 6 | 896 & var33 << 6;
         int var41 = 0;
         ComposerKt.sourceInformationMarkerStart($composer, -553112988, "CC(ReusableComposeNode)N(factory,update,content)410@16187L9:Composables.kt#9igjgp");
         if (!($composer.getApplier() instanceof Applier)) {
            ComposablesKt.invalidApplier();
         }

         $composer.startReusableNode();
         if ($composer.getInserting()) {
            $composer.createNode(var38);
         } else {
            $composer.useNode();
         }

         Composer var42 = Updater.constructor-impl($composer);
         int var43 = 0;
         Updater.set-impl(var42, var29, ComposeUiNode.Companion.getSetMeasurePolicy());
         Updater.set-impl(var42, var36, ComposeUiNode.Companion.getSetResolvedCompositionLocals());
         Updater.set-impl(var42, var35, ComposeUiNode.Companion.getSetCompositeKeyHash());
         Updater.reconcile-impl(var42, ComposeUiNode.Companion.getApplyOnDeactivatedNodeAssertion());
         Updater.set-impl(var42, var37, ComposeUiNode.Companion.getSetModifier());
         int var44 = 14 & var40 >> 6;
         int var46 = 0;
         ComposerKt.sourceInformationMarkerStart($composer, 1833054614, "C72@3469L9:Box.kt#2w3rfo");
         int var47 = 6 | 112 & var77 >> 6;
         BoxScope var84 = (BoxScope)BoxScopeInstance.INSTANCE;
         int var50 = 0;
         ComposerKt.sourceInformationMarkerStart($composer, -2085406509, "C394@18682L3102,390@18504L3280:DetailComponents.kt#wxf82o");
         RoundedCornerShape var51 = RoundedCornerShapeKt.getCircleShape();
         BorderStroke var85;
         if (borderColor != null) {
            double var52 = (double)1.5F;
            int var54 = 0;
            var85 = BorderStrokeKt.BorderStroke-cXLIe8U(Dp.constructor-impl((float)var52), borderColor.unbox-impl());
         } else {
            var85 = null;
         }

         BorderStroke var55 = var85;
         SurfaceKt.Surface-T9BRK9s((Modifier)null, (Shape)var51, containerColor, 0L, 0.0F, 0.0F, var55, (Function2)ComposableLambdaKt.rememberComposableLambda(1570753851, true, DetailComponentsKt::CloudSnippetItem$lambda$57$lambda$56, $composer, 54), $composer, 12582912, 57);
         ComposerKt.sourceInformationMarkerEnd($composer);
         ComposerKt.sourceInformationMarkerEnd($composer);
         $composer.endNode();
         ComposerKt.sourceInformationMarkerEnd($composer);
         ComposerKt.sourceInformationMarkerEnd($composer);
         ComposerKt.sourceInformationMarkerEnd($composer);
         if (ComposerKt.isTraceInProgress()) {
            ComposerKt.traceEventEnd();
         }
      } else {
         $composer.skipToGroupEnd();
      }

      ScopeUpdateScope var86 = $composer.endRestartGroup();
      if (var86 != null) {
         var86.updateScope(DetailComponentsKt::CloudSnippetItem$lambda$58);
      }

   }

   @Composable
   @ComposableTarget(
      applier = "androidx.compose.ui.UiComposable"
   )
   public static final void ActionIcon_iJQMabo/* $FF was: ActionIcon-iJQMabo*/(@NotNull ImageVector icon, long tint, @NotNull Function0<Unit> onClick, @Nullable Composer $composer, int $changed) {
      Intrinsics.checkNotNullParameter(icon, "icon");
      Intrinsics.checkNotNullParameter(onClick, "onClick");
      $composer = $composer.startRestartGroup(1226743047);
      ComposerKt.sourceInformation($composer, "C(ActionIcon)N(icon,tint:c#ui.graphics.Color,onClick)445@21916L103:DetailComponents.kt#wxf82o");
      int $dirty = $changed;
      if (($changed & 6) == 0) {
         $dirty = $changed | ($composer.changed(icon) ? 4 : 2);
      }

      if (($changed & 48) == 0) {
         $dirty |= $composer.changed(tint) ? 32 : 16;
      }

      if (($changed & 384) == 0) {
         $dirty |= $composer.changedInstance(onClick) ? 256 : 128;
      }

      if ($composer.shouldExecute(($dirty & 147) != 146, $dirty & 1)) {
         if (ComposerKt.isTraceInProgress()) {
            ComposerKt.traceEventStart(1226743047, $dirty, -1, "com.android.snippets.ui.ActionIcon (DetailComponents.kt:444)");
         }

         AnimatedCookieButtonKt.AnimatedCookieButton-82CdiHg(onClick, icon, (Modifier)null, (String)null, (String)null, 0L, tint, 0.0F, (Shape)null, false, false, false, false, 0.0F, $composer, 14 & $dirty >> 6 | 112 & $dirty << 3 | 3670016 & $dirty << 15, 0, 16316);
         if (ComposerKt.isTraceInProgress()) {
            ComposerKt.traceEventEnd();
         }
      } else {
         $composer.skipToGroupEnd();
      }

      ScopeUpdateScope var10000 = $composer.endRestartGroup();
      if (var10000 != null) {
         var10000.updateScope(DetailComponentsKt::ActionIcon_iJQMabo$lambda$59);
      }

   }

   @Composable
   @ComposableTarget(
      applier = "androidx.compose.ui.UiComposable"
   )
   public static final void HeaderActionButton_iJQMabo/* $FF was: HeaderActionButton-iJQMabo*/(@NotNull ImageVector icon, long color, @NotNull Function0<Unit> onClick, @Nullable Composer $composer, int $changed) {
      Intrinsics.checkNotNullParameter(icon, "icon");
      Intrinsics.checkNotNullParameter(onClick, "onClick");
      $composer = $composer.startRestartGroup(1154950893);
      ComposerKt.sourceInformation($composer, "C(HeaderActionButton)N(icon,color:c#ui.graphics.Color,onClick)454@22175L7,459@22322L105,463@22434L159,455@22187L406:DetailComponents.kt#wxf82o");
      int $dirty = $changed;
      if (($changed & 6) == 0) {
         $dirty = $changed | ($composer.changed(icon) ? 4 : 2);
      }

      if (($changed & 48) == 0) {
         $dirty |= $composer.changed(color) ? 32 : 16;
      }

      if (($changed & 384) == 0) {
         $dirty |= $composer.changedInstance(onClick) ? 256 : 128;
      }

      if ($composer.shouldExecute(($dirty & 147) != 146, $dirty & 1)) {
         if (ComposerKt.isTraceInProgress()) {
            ComposerKt.traceEventStart(1154950893, $dirty, -1, "com.android.snippets.ui.HeaderActionButton (DetailComponents.kt:453)");
         }

         CompositionLocal var8 = (CompositionLocal)AndroidCompositionLocals_androidKt.getLocalView();
         int var10 = 0;
         int var11 = 0;
         ComposerKt.sourceInformationMarkerStart($composer, 2023513938, "CC(<get-current>):CompositionLocal.kt#9igjgp");
         Object var12 = $composer.consume(var8);
         ComposerKt.sourceInformationMarkerEnd($composer);
         View view = (View)var12;
         Modifier var10000 = (Modifier)Modifier.Companion;
         int var9 = 44;
         var10 = 0;
         Modifier var21 = SizeKt.size-3ABfNKs(var10000, Dp.constructor-impl((float)var9));
         RoundedCornerShape var22 = RoundedCornerShapeKt.getCircleShape();
         long var24 = Color.Companion.getTransparent-0d7_KjU();
         ComposerKt.sourceInformationMarkerStart($composer, 1806255478, "CC(remember):DetailComponents.kt#9igjgp");
         boolean var14 = $composer.changedInstance(view) | ($dirty & 896) == 256;
         int var15 = 0;
         Object var16 = $composer.rememberedValue();
         int var17 = 0;
         Object var26;
         if (!var14 && var16 != Composer.Companion.getEmpty()) {
            var26 = var16;
         } else {
            int var18 = 0;
            Object var19 = DetailComponentsKt::HeaderActionButton_iJQMabo$lambda$61$lambda$60;
            $composer.updateRememberedValue(var19);
            var26 = var19;
         }

         Function0 var25 = (Function0)var26;
         ComposerKt.sourceInformationMarkerEnd($composer);
         SurfaceKt.Surface-o_FOJdg(var25, var21, false, (Shape)var22, var24, 0L, 0.0F, 0.0F, (BorderStroke)null, (MutableInteractionSource)null, (Function2)ComposableLambdaKt.rememberComposableLambda(1869665858, true, DetailComponentsKt::HeaderActionButton_iJQMabo$lambda$63, $composer, 54), $composer, 24624, 6, 996);
         if (ComposerKt.isTraceInProgress()) {
            ComposerKt.traceEventEnd();
         }
      } else {
         $composer.skipToGroupEnd();
      }

      ScopeUpdateScope var27 = $composer.endRestartGroup();
      if (var27 != null) {
         var27.updateScope(DetailComponentsKt::HeaderActionButton_iJQMabo$lambda$64);
      }

   }

   @Composable
   @ComposableTarget(
      applier = "androidx.compose.ui.UiComposable"
   )
   public static final void CurrentSnippetsModal(@NotNull Photo photo, @NotNull Function1<? super String, Unit> onRemove, @NotNull Function0<Unit> onClose, @NotNull SnippetsViewModel viewModel, @Nullable Composer $composer, int $changed) {
      Intrinsics.checkNotNullParameter(photo, "photo");
      Intrinsics.checkNotNullParameter(onRemove, "onRemove");
      Intrinsics.checkNotNullParameter(onClose, "onClose");
      Intrinsics.checkNotNullParameter(viewModel, "viewModel");
      $composer = $composer.startRestartGroup(222168738);
      ComposerKt.sourceInformation($composer, "C(CurrentSnippetsModal)N(photo,onRemove,onClose,viewModel)477@22867L59,478@22952L7,483@23061L192,480@22969L7063:DetailComponents.kt#wxf82o");
      int $dirty = $changed;
      if (($changed & 6) == 0) {
         $dirty = $changed | ($composer.changedInstance(photo) ? 4 : 2);
      }

      if (($changed & 48) == 0) {
         $dirty |= $composer.changedInstance(onRemove) ? 32 : 16;
      }

      if (($changed & 384) == 0) {
         $dirty |= $composer.changedInstance(onClose) ? 256 : 128;
      }

      if (($changed & 3072) == 0) {
         $dirty |= $composer.changedInstance(viewModel) ? 2048 : 1024;
      }

      if ($composer.shouldExecute(($dirty & 1171) != 1170, $dirty & 1)) {
         if (ComposerKt.isTraceInProgress()) {
            ComposerKt.traceEventStart(222168738, $dirty, -1, "com.android.snippets.ui.CurrentSnippetsModal (DetailComponents.kt:476)");
         }

         List var8 = photo.getSnippets();
         ComposerKt.sourceInformationMarkerStart($composer, -1244200067, "CC(remember):DetailComponents.kt#9igjgp");
         boolean var11 = $composer.changed(var8);
         int var12 = 0;
         Object var13 = $composer.rememberedValue();
         int var14 = 0;
         Object var10000;
         if (!var11 && var13 != Composer.Companion.getEmpty()) {
            var10000 = var13;
         } else {
            int var15 = 0;
            Object var16 = SnapshotStateKt.mutableStateOf$default(photo.getSnippets(), (SnapshotMutationPolicy)null, 2, (Object)null);
            $composer.updateRememberedValue(var16);
            var10000 = var16;
         }

         MutableState var9 = (MutableState)var10000;
         ComposerKt.sourceInformationMarkerEnd($composer);
         MutableState localSnippets$delegate = var9;
         CompositionLocal var65 = (CompositionLocal)AndroidCompositionLocals_androidKt.getLocalView();
         var11 = (boolean)0;
         var12 = 0;
         ComposerKt.sourceInformationMarkerStart($composer, 2023513938, "CC(<get-current>):CompositionLocal.kt#9igjgp");
         var13 = $composer.consume(var65);
         ComposerKt.sourceInformationMarkerEnd($composer);
         View view = (View)var13;
         Modifier var116 = SizeKt.fillMaxSize$default((Modifier)Modifier.Companion, 0.0F, 1, (Object)null);
         Unit var10001 = Unit.INSTANCE;
         ComposerKt.sourceInformationMarkerStart($composer, -1244193726, "CC(remember):DetailComponents.kt#9igjgp");
         var11 = $composer.changedInstance(view) | ($dirty & 896) == 256;
         var12 = 0;
         var13 = $composer.rememberedValue();
         var14 = 0;
         Object var10002;
         if (!var11 && var13 != Composer.Companion.getEmpty()) {
            var10002 = var13;
         } else {
            Unit var61 = var10001;
            Modifier var60 = var116;
            int var78 = 0;
            PointerInputEventHandler var16_4 = (PointerInputEventHandler)(new CurrentSnippetsModal.1.1(view, onClose));
            var116 = var60;
            var10001 = var61;
            $composer.updateRememberedValue(var16_4);
            var10002 = var16_4;
         }

         PointerInputEventHandler var66 = (PointerInputEventHandler)var10002;
         ComposerKt.sourceInformationMarkerEnd($composer);
         Modifier var67 = SuspendingPointerInputFilterKt.pointerInput(var116, var10001, var66);
         Alignment var10 = Alignment.Companion.getCenter();
         byte var75 = 48;
         var14 = 0;
         ComposerKt.sourceInformationMarkerStart($composer, 1042775818, "CC(Box)N(modifier,contentAlignment,propagateMinConstraints,content)71@3424L131:Box.kt#2w3rfo");
         var11 = (boolean)0;
         MeasurePolicy var79 = BoxKt.maybeCachedBoxMeasurePolicy(var10, var11);
         int var19 = 112 & var75 << 3;
         int var20 = 0;
         ComposerKt.sourceInformationMarkerStart($composer, -1159599143, "CC(Layout)N(content,modifier,measurePolicy)81@3355L27,84@3521L415:Layout.kt#80mrfh");
         int var21 = Long.hashCode(ComposablesKt.getCurrentCompositeKeyHashCode($composer, 0));
         CompositionLocalMap var22 = $composer.getCurrentCompositionLocalMap();
         Modifier var23 = ComposedModifierKt.materializeModifier($composer, var67);
         Function0 var24 = ComposeUiNode.Companion.getConstructor();
         int var26 = 6 | 896 & var19 << 6;
         int var27 = 0;
         ComposerKt.sourceInformationMarkerStart($composer, -553112988, "CC(ReusableComposeNode)N(factory,update,content)410@16187L9:Composables.kt#9igjgp");
         if (!($composer.getApplier() instanceof Applier)) {
            ComposablesKt.invalidApplier();
         }

         $composer.startReusableNode();
         if ($composer.getInserting()) {
            $composer.createNode(var24);
         } else {
            $composer.useNode();
         }

         Composer var28 = Updater.constructor-impl($composer);
         int var29 = 0;
         Updater.set-impl(var28, var79, ComposeUiNode.Companion.getSetMeasurePolicy());
         Updater.set-impl(var28, var22, ComposeUiNode.Companion.getSetResolvedCompositionLocals());
         Updater.set-impl(var28, var21, ComposeUiNode.Companion.getSetCompositeKeyHash());
         Updater.reconcile-impl(var28, ComposeUiNode.Companion.getApplyOnDeactivatedNodeAssertion());
         Updater.set-impl(var28, var23, ComposeUiNode.Companion.getSetModifier());
         int var30 = 14 & var26 >> 6;
         int var32 = 0;
         ComposerKt.sourceInformationMarkerStart($composer, 1833054614, "C72@3469L9:Box.kt#2w3rfo");
         int var33 = 6 | 112 & var75 >> 6;
         BoxScope var117 = (BoxScope)BoxScopeInstance.INSTANCE;
         int var36 = 0;
         ComposerKt.sourceInformationMarkerStart($composer, 573026448, "C491@23315L137,497@23482L34,498@23546L43,498@23525L64,503@23720L12,501@23617L180,509@23897L11,514@24092L101,519@24262L39,521@24359L67,523@24474L5552,507@23807L6219:DetailComponents.kt#wxf82o");
         BoxKt.Box(BackgroundKt.background-bw27NRU$default(SizeKt.fillMaxSize$default((Modifier)Modifier.Companion, 0.0F, 1, (Object)null), Color.copy-wmQWz5c$default(Color.Companion.getBlack-0d7_KjU(), 0.5F, 0.0F, 0.0F, 0.0F, 14, (Object)null), (Shape)null, 2, (Object)null), $composer, 6);
         ComposerKt.sourceInformationMarkerStart($composer, -1089895266, "CC(remember):DetailComponents.kt#9igjgp");
         boolean var38 = (boolean)0;
         int var39 = 0;
         Object var40 = $composer.rememberedValue();
         int var41 = 0;
         Object var118;
         if (var40 == Composer.Companion.getEmpty()) {
            int var42 = 0;
            Object var93 = SnapshotStateKt.mutableStateOf$default(0.92F, (SnapshotMutationPolicy)null, 2, (Object)null);
            $composer.updateRememberedValue(var93);
            var118 = var93;
         } else {
            var118 = var40;
         }

         MutableState var43 = (MutableState)var118;
         ComposerKt.sourceInformationMarkerEnd($composer);
         MutableState var44 = var43;
         Unit var119 = Unit.INSTANCE;
         ComposerKt.sourceInformationMarkerStart($composer, -1089893209, "CC(remember):DetailComponents.kt#9igjgp");
         var38 = (boolean)0;
         var39 = 0;
         var40 = $composer.rememberedValue();
         var41 = 0;
         Object var126;
         if (var40 == Composer.Companion.getEmpty()) {
            Unit var45 = var119;
            int var94 = 0;
            Function2 var125 = (Function2)(new CurrentSnippetsModal.2.1.1(var43, (Continuation)null));
            var119 = var45;
            Object var46 = var125;
            $composer.updateRememberedValue(var46);
            var126 = var46;
         } else {
            var126 = var40;
         }

         Function2 var98 = (Function2)var126;
         ComposerKt.sourceInformationMarkerEnd($composer);
         EffectsKt.LaunchedEffect(var119, var98, $composer, 6);
         State var99 = AnimateAsStateKt.animateFloatAsState(CurrentSnippetsModal$lambda$94$lambda$70(var44), (AnimationSpec)MaterialTheme.INSTANCE.getMotionScheme($composer, MaterialTheme.$stable).defaultSpatialSpec(), 0.0F, "card_scale", (Function1)null, $composer, 3072, 20);
         var38 = (boolean)48;
         var39 = 0;
         RoundedCornerShape var37 = RoundedCornerShapeKt.RoundedCornerShape-0680j_4(Dp.constructor-impl((float)var38));
         long var47 = MaterialTheme.INSTANCE.getColorScheme($composer, MaterialTheme.$stable).getBackground-0d7_KjU();
         Modifier var120 = SizeKt.fillMaxWidth((Modifier)Modifier.Companion, 0.94F);
         var41 = 16;
         int var95 = 0;
         var120 = PaddingKt.padding-3ABfNKs(var120, Dp.constructor-impl((float)var41));
         var41 = 400;
         var95 = 0;
         var120 = SizeKt.widthIn-VpY3zN4$default(var120, 0.0F, Dp.constructor-impl((float)var41), 1, (Object)null);
         ComposerKt.sourceInformationMarkerStart($composer, -1089875679, "CC(remember):DetailComponents.kt#9igjgp");
         boolean var103 = $composer.changed(var99);
         int var49 = 0;
         Object var50 = $composer.rememberedValue();
         int var51 = 0;
         if (!var103 && var50 != Composer.Companion.getEmpty()) {
            var126 = var50;
         } else {
            Modifier var100 = var120;
            int var52 = 0;
            Function1 var127 = DetailComponentsKt::CurrentSnippetsModal$lambda$94$lambda$75$lambda$74;
            var120 = var100;
            Object var53 = var127;
            $composer.updateRememberedValue(var53);
            var126 = var53;
         }

         Function1 var89 = (Function1)var126;
         ComposerKt.sourceInformationMarkerEnd($composer);
         var120 = GraphicsLayerModifierKt.graphicsLayer(var120, var89);
         ComposerKt.sourceInformationMarkerStart($composer, -1089870301, "CC(remember):DetailComponents.kt#9igjgp");
         var103 = (boolean)0;
         var49 = 0;
         var50 = $composer.rememberedValue();
         var51 = 0;
         if (var50 == Composer.Companion.getEmpty()) {
            Modifier var101 = var120;
            int var113 = 0;
            MutableInteractionSource var129 = InteractionSourceKt.MutableInteractionSource();
            var120 = var101;
            Object var114 = var129;
            $composer.updateRememberedValue(var114);
            var126 = var114;
         } else {
            var126 = var50;
         }

         MutableInteractionSource var90 = (MutableInteractionSource)var126;
         ComposerKt.sourceInformationMarkerEnd($composer);
         MutableInteractionSource var131 = var90;
         var10002 = null;
         boolean var10003 = false;
         Object var10004 = null;
         Object var10005 = null;
         ComposerKt.sourceInformationMarkerStart($composer, -1089867169, "CC(remember):DetailComponents.kt#9igjgp");
         var103 = (boolean)0;
         var49 = 0;
         var50 = $composer.rememberedValue();
         var51 = 0;
         Object var10006;
         if (var50 == Composer.Companion.getEmpty()) {
            Object var54 = null;
            Object var55 = null;
            boolean var56 = false;
            Object var57 = null;
            Modifier var102 = var120;
            int var115 = 0;
            Function0 var52_7 = DetailComponentsKt::CurrentSnippetsModal$lambda$94$lambda$78$lambda$77;
            var120 = var102;
            var131 = var90;
            var10002 = var57;
            var10003 = var56;
            var10004 = var55;
            var10005 = var54;
            $composer.updateRememberedValue(var52_7);
            var10006 = var52_7;
         } else {
            var10006 = var50;
         }

         Function0 var91 = (Function0)var10006;
         ComposerKt.sourceInformationMarkerEnd($composer);
         Modifier var85 = ClickableKt.clickable-O2vRcR0$default(var120, var131, (Indication)var10002, var10003, (String)var10004, (Role)var10005, var91, 28, (Object)null);
         var95 = 16;
         var103 = (boolean)0;
         float var92 = Dp.constructor-impl((float)var95);
         SurfaceKt.Surface-T9BRK9s(var85, (Shape)var37, var47, 0L, 0.0F, var92, (BorderStroke)null, (Function2)ComposableLambdaKt.rememberComposableLambda(-1801854879, true, DetailComponentsKt::CurrentSnippetsModal$lambda$94$lambda$93, $composer, 54), $composer, 12779520, 88);
         ComposerKt.sourceInformationMarkerEnd($composer);
         ComposerKt.sourceInformationMarkerEnd($composer);
         $composer.endNode();
         ComposerKt.sourceInformationMarkerEnd($composer);
         ComposerKt.sourceInformationMarkerEnd($composer);
         ComposerKt.sourceInformationMarkerEnd($composer);
         if (ComposerKt.isTraceInProgress()) {
            ComposerKt.traceEventEnd();
         }
      } else {
         $composer.skipToGroupEnd();
      }

      ScopeUpdateScope var124 = $composer.endRestartGroup();
      if (var124 != null) {
         var124.updateScope(DetailComponentsKt::CurrentSnippetsModal$lambda$95);
      }

   }

   @Composable
   @ComposableTarget(
      applier = "androidx.compose.ui.UiComposable"
   )
   public static final void AddSnippetsModal(@NotNull Photo photo, @NotNull Function3<? super String, ? super Integer, ? super SnippetStyle, Unit> onAdd, @NotNull Function0<Unit> onClose, @NotNull SnippetsViewModel viewModel, @Nullable Composer $composer, int $changed) {
      Intrinsics.checkNotNullParameter(photo, "photo");
      Intrinsics.checkNotNullParameter(onAdd, "onAdd");
      Intrinsics.checkNotNullParameter(onClose, "onClose");
      Intrinsics.checkNotNullParameter(viewModel, "viewModel");
      $composer = $composer.startRestartGroup(-864832516);
      ComposerKt.sourceInformation($composer, "C(AddSnippetsModal)N(photo,onAdd,onClose,viewModel)615@30395L7,616@30419L31,617@30476L39,618@30541L80,620@30648L58,623@30794L490,643@31433L11,644@31508L10,645@31525L17939,641@31340L18124:DetailComponents.kt#wxf82o");
      int $dirty = $changed;
      if (($changed & 6) == 0) {
         $dirty = $changed | ($composer.changedInstance(photo) ? 4 : 2);
      }

      if (($changed & 48) == 0) {
         $dirty |= $composer.changedInstance(onAdd) ? 32 : 16;
      }

      if (($changed & 384) == 0) {
         $dirty |= $composer.changedInstance(onClose) ? 256 : 128;
      }

      if (($changed & 3072) == 0) {
         $dirty |= $composer.changedInstance(viewModel) ? 2048 : 1024;
      }

      if ($composer.shouldExecute(($dirty & 1171) != 1170, $dirty & 1)) {
         if (ComposerKt.isTraceInProgress()) {
            ComposerKt.traceEventStart(-864832516, $dirty, -1, "com.android.snippets.ui.AddSnippetsModal (DetailComponents.kt:614)");
         }

         CompositionLocal var8 = (CompositionLocal)AndroidCompositionLocals_androidKt.getLocalView();
         int var10 = 0;
         int var11 = 0;
         ComposerKt.sourceInformationMarkerStart($composer, 2023513938, "CC(<get-current>):CompositionLocal.kt#9igjgp");
         Object var12 = $composer.consume(var8);
         ComposerKt.sourceInformationMarkerEnd($composer);
         View view = (View)var12;
         ComposerKt.sourceInformationMarkerStart($composer, 2119619835, "CC(remember):DetailComponents.kt#9igjgp");
         var11 = 0;
         int var30 = 0;
         Object var13 = $composer.rememberedValue();
         int var14 = 0;
         Object var10000;
         if (var13 == Composer.Companion.getEmpty()) {
            int var15 = 0;
            Object var43 = SnapshotStateKt.mutableStateOf$default("", (SnapshotMutationPolicy)null, 2, (Object)null);
            $composer.updateRememberedValue(var43);
            var10000 = var43;
         } else {
            var10000 = var13;
         }

         MutableState selectedColor$delegate = (MutableState)var10000;
         ComposerKt.sourceInformationMarkerEnd($composer);
         MutableState text$delegate = selectedColor$delegate;
         ComposerKt.sourceInformationMarkerStart($composer, 2119621667, "CC(remember):DetailComponents.kt#9igjgp");
         var30 = 0;
         int var34 = 0;
         Object var38 = $composer.rememberedValue();
         int var44 = 0;
         if (var38 == Composer.Companion.getEmpty()) {
            int var16 = 0;
            Object var47 = SnapshotStateKt.mutableStateOf$default((Object)null, (SnapshotMutationPolicy)null, 2, (Object)null);
            $composer.updateRememberedValue(var47);
            var10000 = var47;
         } else {
            var10000 = var38;
         }

         MutableState selectedStyle$delegate = (MutableState)var10000;
         ComposerKt.sourceInformationMarkerEnd($composer);
         selectedColor$delegate = selectedStyle$delegate;
         ComposerKt.sourceInformationMarkerStart($composer, 2119623788, "CC(remember):DetailComponents.kt#9igjgp");
         var34 = 0;
         int var39 = 0;
         Object var45 = $composer.rememberedValue();
         int var48 = 0;
         if (var45 == Composer.Companion.getEmpty()) {
            int var17 = 0;
            Object var51 = SnapshotStateKt.mutableStateOf$default(SnippetStyle.Default, (SnapshotMutationPolicy)null, 2, (Object)null);
            $composer.updateRememberedValue(var51);
            var10000 = var51;
         } else {
            var10000 = var45;
         }

         MutableState var28 = (MutableState)var10000;
         ComposerKt.sourceInformationMarkerEnd($composer);
         selectedStyle$delegate = var28;
         ComposerKt.sourceInformationMarkerStart($composer, 2119627190, "CC(remember):DetailComponents.kt#9igjgp");
         var39 = 0;
         int var46 = 0;
         Object var49 = $composer.rememberedValue();
         int var52 = 0;
         if (var49 == Composer.Companion.getEmpty()) {
            int var18 = 0;
            Object var54 = SnapshotIntStateKt.mutableIntStateOf(0);
            $composer.updateRememberedValue(var54);
            var10000 = var54;
         } else {
            var10000 = var49;
         }

         MutableIntState options = (MutableIntState)var10000;
         ComposerKt.sourceInformationMarkerEnd($composer);
         MutableIntState selectedIndex$delegate = options;
         String[] var36 = new String[]{"Text", "Color", "Style"};
         List options = CollectionsKt.listOf(var36);
         ComposerKt.sourceInformationMarkerStart($composer, 2119632294, "CC(remember):DetailComponents.kt#9igjgp");
         boolean var50 = false;
         var52 = 0;
         Object var55 = $composer.rememberedValue();
         int var19 = 0;
         if (var55 == Composer.Companion.getEmpty()) {
            int var20 = 0;
            Integer[] var21 = new Integer[]{-1092784, -1294214, -5552196, -12409355, -14244198, -10044566, -4520, -22746, -7508381, -8875876, -2825897};
            Object var56 = CollectionsKt.listOf(var21);
            $composer.updateRememberedValue(var56);
            var10000 = var56;
         } else {
            var10000 = var55;
         }

         List localSnippetsCount = (List)var10000;
         ComposerKt.sourceInformationMarkerEnd($composer);
         List snippetColorsPalette = localSnippetsCount;
         int localSnippetsCount = photo.getSnippets().size();
         ModalBottomSheetKt.ModalBottomSheet-YbuCTN8(onClose, (Modifier)null, (SheetState)null, 0.0F, false, (Shape)null, MaterialTheme.INSTANCE.getColorScheme($composer, MaterialTheme.$stable).getSurfaceContainerHigh-0d7_KjU(), 0L, 0.0F, BottomSheetDefaults.INSTANCE.getScrimColor($composer, 6), (Function2)null, (Function2)null, (ModalBottomSheetProperties)null, (Function3)ComposableLambdaKt.rememberComposableLambda(-1693328034, true, DetailComponentsKt::AddSnippetsModal$lambda$158, $composer, 54), $composer, 14 & $dirty >> 6, 3072, 7614);
         if (ComposerKt.isTraceInProgress()) {
            ComposerKt.traceEventEnd();
         }
      } else {
         $composer.skipToGroupEnd();
      }

      ScopeUpdateScope var61 = $composer.endRestartGroup();
      if (var61 != null) {
         var61.updateScope(DetailComponentsKt::AddSnippetsModal$lambda$159);
      }

   }

   @Composable
   @ComposableTarget(
      applier = "androidx.compose.ui.UiComposable"
   )
   public static final void CanvasBackgroundDialog(@NotNull CanvasAction action, @NotNull Function0<Unit> onDismiss, @NotNull Function1<? super Boolean, Unit> onConfirm, @Nullable Composer $composer, int $changed) {
      Intrinsics.checkNotNullParameter(action, "action");
      Intrinsics.checkNotNullParameter(onDismiss, "onDismiss");
      Intrinsics.checkNotNullParameter(onConfirm, "onConfirm");
      $composer = $composer.startRestartGroup(2026416772);
      ComposerKt.sourceInformation($composer, "C(CanvasBackgroundDialog)N(action,onDismiss,onConfirm)932@49669L7,933@49703L34,935@49844L3644,935@49743L3745:DetailComponents.kt#wxf82o");
      int $dirty = $changed;
      if (($changed & 6) == 0) {
         $dirty = $changed | ($composer.changed(((Enum)action).ordinal()) ? 4 : 2);
      }

      if (($changed & 48) == 0) {
         $dirty |= $composer.changedInstance(onDismiss) ? 32 : 16;
      }

      if (($changed & 384) == 0) {
         $dirty |= $composer.changedInstance(onConfirm) ? 256 : 128;
      }

      if ($composer.shouldExecute(($dirty & 147) != 146, $dirty & 1)) {
         if (ComposerKt.isTraceInProgress()) {
            ComposerKt.traceEventStart(2026416772, $dirty, -1, "com.android.snippets.ui.CanvasBackgroundDialog (DetailComponents.kt:931)");
         }

         CompositionLocal var7 = (CompositionLocal)AndroidCompositionLocals_androidKt.getLocalView();
         int var9 = 0;
         int var10 = 0;
         ComposerKt.sourceInformationMarkerStart($composer, 2023513938, "CC(<get-current>):CompositionLocal.kt#9igjgp");
         Object var11 = $composer.consume(var7);
         ComposerKt.sourceInformationMarkerEnd($composer);
         View view = (View)var11;
         ComposerKt.sourceInformationMarkerStart($composer, 1766338886, "CC(remember):DetailComponents.kt#9igjgp");
         var10 = 0;
         int var17 = 0;
         Object var12 = $composer.rememberedValue();
         int var13 = 0;
         Object var10000;
         if (var12 == Composer.Companion.getEmpty()) {
            int var14 = 0;
            Object var18 = SnapshotStateKt.mutableStateOf$default(false, (SnapshotMutationPolicy)null, 2, (Object)null);
            $composer.updateRememberedValue(var18);
            var10000 = var18;
         } else {
            var10000 = var12;
         }

         MutableState isDarkSelected$delegate = (MutableState)var10000;
         ComposerKt.sourceInformationMarkerEnd($composer);
         AndroidDialog_androidKt.Dialog(onDismiss, new DialogProperties(false, false, false, 3, (DefaultConstructorMarker)null), (Function2)ComposableLambdaKt.rememberComposableLambda(-2008299493, true, DetailComponentsKt::CanvasBackgroundDialog$lambda$173, $composer, 54), $composer, 432 | 14 & $dirty >> 3, 0);
         if (ComposerKt.isTraceInProgress()) {
            ComposerKt.traceEventEnd();
         }
      } else {
         $composer.skipToGroupEnd();
      }

      ScopeUpdateScope var19 = $composer.endRestartGroup();
      if (var19 != null) {
         var19.updateScope(DetailComponentsKt::CanvasBackgroundDialog$lambda$174);
      }

   }

   @Composable
   @ComposableTarget(
      applier = "androidx.compose.ui.UiComposable"
   )
   public static final void CanvasOptionCard(@NotNull String title, boolean isDark, boolean isSelected, @NotNull Function0<Unit> onClick, @Nullable Modifier modifier, @Nullable Composer $composer, int $changed, int var7) {
      Intrinsics.checkNotNullParameter(title, "title");
      Intrinsics.checkNotNullParameter(onClick, "onClick");
      $composer = $composer.startRestartGroup(2018085666);
      ComposerKt.sourceInformation($composer, "C(CanvasOptionCard)N(title,isDark,isSelected,onClick,modifier)1040@54263L1806,1034@54030L2039:DetailComponents.kt#wxf82o");
      int $dirty = $changed;
      if (($changed & 6) == 0) {
         $dirty = $changed | ($composer.changed(title) ? 4 : 2);
      }

      if (($changed & 48) == 0) {
         $dirty |= $composer.changed(isDark) ? 32 : 16;
      }

      if (($changed & 384) == 0) {
         $dirty |= $composer.changed(isSelected) ? 256 : 128;
      }

      if (($changed & 3072) == 0) {
         $dirty |= $composer.changedInstance(onClick) ? 2048 : 1024;
      }

      if ((var7 & 16) != 0) {
         $dirty |= 24576;
      } else if (($changed & 24576) == 0) {
         $dirty |= $composer.changed(modifier) ? 16384 : 8192;
      }

      if ($composer.shouldExecute(($dirty & 9363) != 9362, $dirty & 1)) {
         if ((var7 & 16) != 0) {
            modifier = (Modifier)Modifier.Companion;
         }

         if (ComposerKt.isTraceInProgress()) {
            ComposerKt.traceEventStart(2018085666, $dirty, -1, "com.android.snippets.ui.CanvasOptionCard (DetailComponents.kt:1029)");
         }

         long var10000;
         if (isSelected) {
            $composer.startReplaceGroup(-1944885527);
            ComposerKt.sourceInformation($composer, "1030@53706L11");
            long var11 = MaterialTheme.INSTANCE.getColorScheme($composer, MaterialTheme.$stable).getPrimary-0d7_KjU();
            $composer.endReplaceGroup();
            var10000 = var11;
         } else {
            $composer.startReplaceGroup(-1944884272);
            ComposerKt.sourceInformation($composer, "1030@53745L11");
            long var18 = MaterialTheme.INSTANCE.getColorScheme($composer, MaterialTheme.$stable).getOutlineVariant-0d7_KjU();
            $composer.endReplaceGroup();
            var10000 = var18;
         }

         long borderColor = var10000;
         if (isSelected) {
            $composer.startReplaceGroup(-1944881324);
            ComposerKt.sourceInformation($composer, "1031@53820L11");
            long var13 = Color.copy-wmQWz5c$default(MaterialTheme.INSTANCE.getColorScheme($composer, MaterialTheme.$stable).getPrimaryContainer-0d7_KjU(), 0.3F, 0.0F, 0.0F, 0.0F, 14, (Object)null);
            $composer.endReplaceGroup();
            var10000 = var13;
         } else {
            $composer.startReplaceGroup(-1944879735);
            ComposerKt.sourceInformation($composer, "1031@53887L11");
            long var20 = MaterialTheme.INSTANCE.getColorScheme($composer, MaterialTheme.$stable).getSurface-0d7_KjU();
            $composer.endReplaceGroup();
            var10000 = var20;
         }

         long bgColor = var10000;
         if (isSelected) {
            $composer.startReplaceGroup(-1944877495);
            ComposerKt.sourceInformation($composer, "1032@53957L11");
            long var15 = MaterialTheme.INSTANCE.getColorScheme($composer, MaterialTheme.$stable).getPrimary-0d7_KjU();
            $composer.endReplaceGroup();
         } else {
            $composer.startReplaceGroup(-1944876238);
            ComposerKt.sourceInformation($composer, "1032@53996L11");
            long var21 = MaterialTheme.INSTANCE.getColorScheme($composer, MaterialTheme.$stable).getOnSurfaceVariant-0d7_KjU();
            $composer.endReplaceGroup();
         }

         Modifier var10001 = AspectRatioKt.aspectRatio$default(modifier, 0.85F, false, 2, (Object)null);
         int var22 = 16;
         int var16 = 0;
         Shape var10003 = (Shape)RoundedCornerShapeKt.RoundedCornerShape-0680j_4(Dp.constructor-impl((float)var22));
         float var10008;
         if (isSelected) {
            var22 = 2;
            var16 = 0;
            var10008 = Dp.constructor-impl((float)var22);
         } else {
            var22 = 1;
            var16 = 0;
            var10008 = Dp.constructor-impl((float)var22);
         }

         SurfaceKt.Surface-o_FOJdg(onClick, var10001, false, var10003, bgColor, 0L, 0.0F, 0.0F, BorderStrokeKt.BorderStroke-cXLIe8U(var10008, borderColor), (MutableInteractionSource)null, (Function2)ComposableLambdaKt.rememberComposableLambda(1213588215, true, DetailComponentsKt::CanvasOptionCard$lambda$178, $composer, 54), $composer, 14 & $dirty >> 9, 6, 740);
         if (ComposerKt.isTraceInProgress()) {
            ComposerKt.traceEventEnd();
         }
      } else {
         $composer.skipToGroupEnd();
      }

      ScopeUpdateScope var28 = $composer.endRestartGroup();
      if (var28 != null) {
         var28.updateScope(DetailComponentsKt::CanvasOptionCard$lambda$179);
      }

   }

   private static final Unit CookieShape$lambda$0(Path $this$GenericShape, Size size, LayoutDirection var2) {
      Intrinsics.checkNotNullParameter($this$GenericShape, "$this$GenericShape");
      Intrinsics.checkNotNullParameter(var2, "<unused var>");
      long var7_1 = size.unbox-impl();
      int var6 = 0;
      int var9 = 0;
      int var10 = (int)(var7_1 >> 32);
      int var11 = 0;
      float width = Float.intBitsToFloat(var10);
      long var8_1 = size.unbox-impl();
      int var7 = 0;
      var10 = 0;
      var11 = (int)(var8_1 & 4294967295L);
      int var12 = 0;
      float height = Float.intBitsToFloat(var11);
      float radius = Math.min(width, height) / 2.0F;
      RoundedPolygon polygon = ShapesKt.star$default(RoundedPolygon.Companion, 12, radius, radius * 0.88F, new CornerRounding(radius * 0.12F, 0.0F, 2, (DefaultConstructorMarker)null), (CornerRounding)null, (List)null, width / 2.0F, height / 2.0F, 48, (Object)null);
      Path.addPath-Uv8p0NA$default($this$GenericShape, AndroidPath_androidKt.asComposePath(Shapes_androidKt.toPath$default(polygon, (android.graphics.Path)null, 1, (Object)null)), 0L, 2, (Object)null);
      return Unit.INSTANCE;
   }

   private static final Unit DetailTopBar$lambda$2$lambda$1() {
      return Unit.INSTANCE;
   }

   private static final Unit DetailTopBar$lambda$4$lambda$3() {
      return Unit.INSTANCE;
   }

   private static final Unit DetailTopBar$lambda$6$lambda$5() {
      return Unit.INSTANCE;
   }

   private static final Unit DetailTopBar$lambda$8$lambda$7() {
      return Unit.INSTANCE;
   }

   private static final int DetailTopBar$lambda$45$lambda$13$lambda$10$lambda$9(int it) {
      return -it / 2;
   }

   private static final int DetailTopBar$lambda$45$lambda$13$lambda$12$lambda$11(int it) {
      return -it / 2;
   }

   private static final Unit DetailTopBar$lambda$45$lambda$44$lambda$16$lambda$15(View $view, Function0 $onAdd) {
      $view.performHapticFeedback(16);
      $onAdd.invoke();
      return Unit.INSTANCE;
   }

   private static final Unit DetailTopBar$lambda$45$lambda$44$lambda$43$lambda$42$lambda$26$lambda$25$lambda$18$lambda$17(View $view, Function0 $onDownload, Function0 $closeMenu) {
      $view.performHapticFeedback(16);
      $onDownload.invoke();
      $closeMenu.invoke();
      return Unit.INSTANCE;
   }

   private static final Unit DetailTopBar$lambda$45$lambda$44$lambda$43$lambda$42$lambda$26$lambda$25$lambda$20$lambda$19(View $view, Function0 $onShare, Function0 $closeMenu) {
      $view.performHapticFeedback(16);
      $onShare.invoke();
      $closeMenu.invoke();
      return Unit.INSTANCE;
   }

   @Composable
   @ComposableTarget(
      applier = "androidx.compose.ui.UiComposable"
   )
   private static final Unit DetailTopBar$lambda$45$lambda$44$lambda$43$lambda$42$lambda$26$lambda$25$lambda$21(boolean $isFavorite, Composer $composer, int $changed) {
      ComposerKt.sourceInformation($composer, "C179@8113L50:DetailComponents.kt#wxf82o");
      if ($composer.shouldExecute(($changed & 3) != 2, $changed & 1)) {
         if (ComposerKt.isTraceInProgress()) {
            ComposerKt.traceEventStart(-1918426695, $changed, -1, "com.android.snippets.ui.DetailTopBar.<anonymous>.<anonymous>.<anonymous>.<anonymous>.<anonymous>.<anonymous>.<anonymous> (DetailComponents.kt:179)");
         }

         TextKt.Text-Nvy7gAk($isFavorite ? "Unfavorite" : "Favorite", (Modifier)null, 0L, (TextAutoSize)null, 0L, (FontStyle)null, (FontWeight)null, (FontFamily)null, 0L, (TextDecoration)null, (TextAlign)null, 0L, 0, false, 0, 0, (Function1)null, (TextStyle)null, $composer, 0, 0, 262142);
         if (ComposerKt.isTraceInProgress()) {
            ComposerKt.traceEventEnd();
         }
      } else {
         $composer.skipToGroupEnd();
      }

      return Unit.INSTANCE;
   }

   private static final Unit DetailTopBar$lambda$45$lambda$44$lambda$43$lambda$42$lambda$26$lambda$25$lambda$23$lambda$22(View $view, Function0 $onToggleFavorite, Function0 $closeMenu) {
      $view.performHapticFeedback(16);
      $onToggleFavorite.invoke();
      $closeMenu.invoke();
      return Unit.INSTANCE;
   }

   @Composable
   @ComposableTarget(
      applier = "androidx.compose.ui.UiComposable"
   )
   private static final Unit DetailTopBar$lambda$45$lambda$44$lambda$43$lambda$42$lambda$26$lambda$25$lambda$24(boolean $isFavorite, Composer $composer, int $changed) {
      ComposerKt.sourceInformation($composer, "C180@8219L84:DetailComponents.kt#wxf82o");
      if ($composer.shouldExecute(($changed & 3) != 2, $changed & 1)) {
         if (ComposerKt.isTraceInProgress()) {
            ComposerKt.traceEventStart(-240115690, $changed, -1, "com.android.snippets.ui.DetailTopBar.<anonymous>.<anonymous>.<anonymous>.<anonymous>.<anonymous>.<anonymous>.<anonymous> (DetailComponents.kt:180)");
         }

         IconKt.Icon-ww6aTOc($isFavorite ? FavoriteKt.getFavorite(Icons.INSTANCE.getDefault()) : FavoriteBorderKt.getFavoriteBorder(Icons.INSTANCE.getDefault()), (String)null, (Modifier)null, 0L, $composer, 48, 12);
         if (ComposerKt.isTraceInProgress()) {
            ComposerKt.traceEventEnd();
         }
      } else {
         $composer.skipToGroupEnd();
      }

      return Unit.INSTANCE;
   }

   @Composable
   @ComposableTarget(
      applier = "androidx.compose.ui.UiComposable"
   )
   private static final Unit DetailTopBar$lambda$45$lambda$44$lambda$43$lambda$42$lambda$26(View $view, Function0 $onDownload, Function0 $closeMenu, boolean $hasSnippets, Function0 $onShare, Function0 $onToggleFavorite, boolean $isFavorite, Composer $composer, int $changed) {
      ComposerKt.sourceInformation($composer, "C165@7144L1428:DetailComponents.kt#wxf82o");
      if ($composer.shouldExecute(($changed & 3) != 2, $changed & 1)) {
         if (ComposerKt.isTraceInProgress()) {
            ComposerKt.traceEventStart(-916838601, $changed, -1, "com.android.snippets.ui.DetailTopBar.<anonymous>.<anonymous>.<anonymous>.<anonymous>.<anonymous> (DetailComponents.kt:165)");
         }

         byte var13 = 0;
         int var14 = 0;
         ComposerKt.sourceInformationMarkerStart($composer, 1341605231, "CC(Column)N(modifier,verticalArrangement,horizontalAlignment,content)87@4443L61,88@4509L134:Column.kt#2w3rfo");
         Modifier var9 = (Modifier)Modifier.Companion;
         Arrangement.Vertical var10 = Arrangement.INSTANCE.getTop();
         Alignment.Horizontal var11 = Alignment.Companion.getStart();
         MeasurePolicy var15 = ColumnKt.columnMeasurePolicy(var10, var11, $composer, 14 & var13 >> 3 | 112 & var13 >> 3);
         int var19 = 112 & var13 << 3;
         int var20 = 0;
         ComposerKt.sourceInformationMarkerStart($composer, -1159599143, "CC(Layout)N(content,modifier,measurePolicy)81@3355L27,84@3521L415:Layout.kt#80mrfh");
         int var21 = Long.hashCode(ComposablesKt.getCurrentCompositeKeyHashCode($composer, 0));
         CompositionLocalMap var22 = $composer.getCurrentCompositionLocalMap();
         Modifier var23 = ComposedModifierKt.materializeModifier($composer, var9);
         Function0 var24 = ComposeUiNode.Companion.getConstructor();
         int var26 = 6 | 896 & var19 << 6;
         int var27 = 0;
         ComposerKt.sourceInformationMarkerStart($composer, -553112988, "CC(ReusableComposeNode)N(factory,update,content)410@16187L9:Composables.kt#9igjgp");
         if (!($composer.getApplier() instanceof Applier)) {
            ComposablesKt.invalidApplier();
         }

         $composer.startReusableNode();
         if ($composer.getInserting()) {
            $composer.createNode(var24);
         } else {
            $composer.useNode();
         }

         Composer var28 = Updater.constructor-impl($composer);
         int var29 = 0;
         Updater.set-impl(var28, var15, ComposeUiNode.Companion.getSetMeasurePolicy());
         Updater.set-impl(var28, var22, ComposeUiNode.Companion.getSetResolvedCompositionLocals());
         Updater.set-impl(var28, var21, ComposeUiNode.Companion.getSetCompositeKeyHash());
         Updater.reconcile-impl(var28, ComposeUiNode.Companion.getApplyOnDeactivatedNodeAssertion());
         Updater.set-impl(var28, var23, ComposeUiNode.Companion.getSetModifier());
         int var30 = 14 & var26 >> 6;
         int var32 = 0;
         ComposerKt.sourceInformationMarkerStart($composer, 2093002350, "C89@4557L9:Column.kt#2w3rfo");
         int var33 = 6 | 112 & var13 >> 6;
         ColumnScope var10000 = (ColumnScope)ColumnScopeInstance.INSTANCE;
         int var36 = 0;
         ComposerKt.sourceInformationMarkerStart($composer, -1816269707, "C170@7467L90,166@7185L406,176@7896L87,172@7624L393,179@8111L54,182@8412L96,180@8217L88,178@8050L492:DetailComponents.kt#wxf82o");
         Function2 var62 = com.android.snippets.ui.ComposableSingletons.DetailComponentsKt.INSTANCE.getLambda$-397019599$app_debug();
         ComposerKt.sourceInformationMarkerStart($composer, -612770917, "CC(remember):DetailComponents.kt#9igjgp");
         boolean var38 = $composer.changedInstance($view) | $composer.changed($onDownload) | $composer.changed($closeMenu);
         int var39 = 0;
         Object var40 = $composer.rememberedValue();
         int var41 = 0;
         Object var65;
         if (!var38 && var40 != Composer.Companion.getEmpty()) {
            var65 = var40;
         } else {
            Function2 var42 = var62;
            int var43 = 0;
            Function0 var10001 = DetailComponentsKt::DetailTopBar$lambda$45$lambda$44$lambda$43$lambda$42$lambda$26$lambda$25$lambda$18$lambda$17;
            var62 = var42;
            Object var44 = var10001;
            $composer.updateRememberedValue(var44);
            var65 = var44;
         }

         Function0 var45 = (Function0)var65;
         ComposerKt.sourceInformationMarkerEnd($composer);
         AndroidMenu_androidKt.DropdownMenuItem(var62, var45, (Modifier)null, com.android.snippets.ui.ComposableSingletons.DetailComponentsKt.INSTANCE.getLambda$1347741646$app_debug(), (Function2)null, $hasSnippets, (MenuItemColors)null, (PaddingValues)null, (MutableInteractionSource)null, $composer, 3078, 468);
         var62 = com.android.snippets.ui.ComposableSingletons.DetailComponentsKt.INSTANCE.getLambda$1476250266$app_debug();
         ComposerKt.sourceInformationMarkerStart($composer, -612757192, "CC(remember):DetailComponents.kt#9igjgp");
         var38 = $composer.changedInstance($view) | $composer.changed($onShare) | $composer.changed($closeMenu);
         var39 = 0;
         var40 = $composer.rememberedValue();
         var41 = 0;
         if (!var38 && var40 != Composer.Companion.getEmpty()) {
            var65 = var40;
         } else {
            Function2 var54 = var62;
            int var56 = 0;
            Function0 var66 = DetailComponentsKt::DetailTopBar$lambda$45$lambda$44$lambda$43$lambda$42$lambda$26$lambda$25$lambda$20$lambda$19;
            var62 = var54;
            Object var58 = var66;
            $composer.updateRememberedValue(var58);
            var65 = var58;
         }

         var45 = (Function0)var65;
         ComposerKt.sourceInformationMarkerEnd($composer);
         AndroidMenu_androidKt.DropdownMenuItem(var62, var45, (Modifier)null, com.android.snippets.ui.ComposableSingletons.DetailComponentsKt.INSTANCE.getLambda$-1140406025$app_debug(), (Function2)null, $hasSnippets, (MenuItemColors)null, (PaddingValues)null, (MutableInteractionSource)null, $composer, 3078, 468);
         var62 = (Function2)ComposableLambdaKt.rememberComposableLambda(-1918426695, true, DetailComponentsKt::DetailTopBar$lambda$45$lambda$44$lambda$43$lambda$42$lambda$26$lambda$25$lambda$21, $composer, 54);
         ComposerKt.sourceInformationMarkerStart($composer, -612740671, "CC(remember):DetailComponents.kt#9igjgp");
         var38 = $composer.changedInstance($view) | $composer.changed($onToggleFavorite) | $composer.changed($closeMenu);
         var39 = 0;
         var40 = $composer.rememberedValue();
         var41 = 0;
         if (!var38 && var40 != Composer.Companion.getEmpty()) {
            var65 = var40;
         } else {
            Function2 var55 = var62;
            int var57 = 0;
            Function0 var68 = DetailComponentsKt::DetailTopBar$lambda$45$lambda$44$lambda$43$lambda$42$lambda$26$lambda$25$lambda$23$lambda$22;
            var62 = var55;
            Object var59 = var68;
            $composer.updateRememberedValue(var59);
            var65 = var59;
         }

         var45 = (Function0)var65;
         ComposerKt.sourceInformationMarkerEnd($composer);
         AndroidMenu_androidKt.DropdownMenuItem(var62, var45, (Modifier)null, (Function2)ComposableLambdaKt.rememberComposableLambda(-240115690, true, DetailComponentsKt::DetailTopBar$lambda$45$lambda$44$lambda$43$lambda$42$lambda$26$lambda$25$lambda$24, $composer, 54), (Function2)null, $hasSnippets, (MenuItemColors)null, (PaddingValues)null, (MutableInteractionSource)null, $composer, 3078, 468);
         ComposerKt.sourceInformationMarkerEnd($composer);
         ComposerKt.sourceInformationMarkerEnd($composer);
         $composer.endNode();
         ComposerKt.sourceInformationMarkerEnd($composer);
         ComposerKt.sourceInformationMarkerEnd($composer);
         ComposerKt.sourceInformationMarkerEnd($composer);
         if (ComposerKt.isTraceInProgress()) {
            ComposerKt.traceEventEnd();
         }
      } else {
         $composer.skipToGroupEnd();
      }

      return Unit.INSTANCE;
   }

   private static final Unit DetailTopBar$lambda$45$lambda$44$lambda$43$lambda$42$lambda$37$lambda$36$lambda$28$lambda$27(View $view, Function0 $onEdit, Function0 $closeMenu) {
      $view.performHapticFeedback(16);
      $onEdit.invoke();
      $closeMenu.invoke();
      return Unit.INSTANCE;
   }

   @Composable
   @ComposableTarget(
      applier = "androidx.compose.ui.UiComposable"
   )
   private static final Unit DetailTopBar$lambda$45$lambda$44$lambda$43$lambda$42$lambda$37$lambda$36$lambda$29(boolean $isPublic, Composer $composer, int $changed) {
      ComposerKt.sourceInformation($composer, "C204@9689L53:DetailComponents.kt#wxf82o");
      if ($composer.shouldExecute(($changed & 3) != 2, $changed & 1)) {
         if (ComposerKt.isTraceInProgress()) {
            ComposerKt.traceEventStart(-2008030232, $changed, -1, "com.android.snippets.ui.DetailTopBar.<anonymous>.<anonymous>.<anonymous>.<anonymous>.<anonymous>.<anonymous>.<anonymous> (DetailComponents.kt:204)");
         }

         TextKt.Text-Nvy7gAk($isPublic ? "Make Private" : "Make Public", (Modifier)null, 0L, (TextAutoSize)null, 0L, (FontStyle)null, (FontWeight)null, (FontFamily)null, 0L, (TextDecoration)null, (TextAlign)null, 0L, 0, false, 0, 0, (Function1)null, (TextStyle)null, $composer, 0, 0, 262142);
         if (ComposerKt.isTraceInProgress()) {
            ComposerKt.traceEventEnd();
         }
      } else {
         $composer.skipToGroupEnd();
      }

      return Unit.INSTANCE;
   }

   private static final Unit DetailTopBar$lambda$45$lambda$44$lambda$43$lambda$42$lambda$37$lambda$36$lambda$31$lambda$30(View $view, Function0 $onTogglePublic, Function0 $closeMenu) {
      $view.performHapticFeedback(16);
      $onTogglePublic.invoke();
      $closeMenu.invoke();
      return Unit.INSTANCE;
   }

   @Composable
   @ComposableTarget(
      applier = "androidx.compose.ui.UiComposable"
   )
   private static final Unit DetailTopBar$lambda$45$lambda$44$lambda$43$lambda$42$lambda$37$lambda$36$lambda$32(boolean $isPublic, Composer $composer, int $changed) {
      ComposerKt.sourceInformation($composer, "C205@9851L124,205@9798L184:DetailComponents.kt#wxf82o");
      if ($composer.shouldExecute(($changed & 3) != 2, $changed & 1)) {
         if (ComposerKt.isTraceInProgress()) {
            ComposerKt.traceEventStart(-329719227, $changed, -1, "com.android.snippets.ui.DetailTopBar.<anonymous>.<anonymous>.<anonymous>.<anonymous>.<anonymous>.<anonymous>.<anonymous> (DetailComponents.kt:205)");
         }

         IconKt.Icon-ww6aTOc(VectorResources_androidKt.vectorResource(ImageVector.Companion, $isPublic ? drawable.ic_private : drawable.ic_public, $composer, 6), (String)null, (Modifier)null, 0L, $composer, 48, 12);
         if (ComposerKt.isTraceInProgress()) {
            ComposerKt.traceEventEnd();
         }
      } else {
         $composer.skipToGroupEnd();
      }

      return Unit.INSTANCE;
   }

   @Composable
   @ComposableTarget(
      applier = "androidx.compose.ui.UiComposable"
   )
   private static final Unit DetailTopBar$lambda$45$lambda$44$lambda$43$lambda$42$lambda$37$lambda$36$lambda$33(boolean $hasLocationLink, Composer $composer, int $changed) {
      ComposerKt.sourceInformation($composer, "C210@10312L54:DetailComponents.kt#wxf82o");
      if ($composer.shouldExecute(($changed & 3) != 2, $changed & 1)) {
         if (ComposerKt.isTraceInProgress()) {
            ComposerKt.traceEventStart(-454688426, $changed, -1, "com.android.snippets.ui.DetailTopBar.<anonymous>.<anonymous>.<anonymous>.<anonymous>.<anonymous>.<anonymous>.<anonymous> (DetailComponents.kt:210)");
         }

         TextKt.Text-Nvy7gAk($hasLocationLink ? "Edit Link" : "Add Link", (Modifier)null, 0L, (TextAutoSize)null, 0L, (FontStyle)null, (FontWeight)null, (FontFamily)null, 0L, (TextDecoration)null, (TextAlign)null, 0L, 0, false, 0, 0, (Function1)null, (TextStyle)null, $composer, 0, 0, 262142);
         if (ComposerKt.isTraceInProgress()) {
            ComposerKt.traceEventEnd();
         }
      } else {
         $composer.skipToGroupEnd();
      }

      return Unit.INSTANCE;
   }

   private static final Unit DetailTopBar$lambda$45$lambda$44$lambda$43$lambda$42$lambda$37$lambda$36$lambda$35$lambda$34(View $view, Function0 $onAddLinkClick, Function0 $closeMenu) {
      $view.performHapticFeedback(16);
      $onAddLinkClick.invoke();
      $closeMenu.invoke();
      return Unit.INSTANCE;
   }

   @Composable
   @ComposableTarget(
      applier = "androidx.compose.ui.UiComposable"
   )
   private static final Unit DetailTopBar$lambda$45$lambda$44$lambda$43$lambda$42$lambda$37(boolean $hasSnippets, View $view, Function0 $onEdit, Function0 $closeMenu, Function0 $onTogglePublic, boolean $isPublic, Function0 $onAddLinkClick, boolean $hasLocationLink, Composer $composer, int $changed) {
      ComposerKt.sourceInformation($composer, "C195@9107L1693:DetailComponents.kt#wxf82o");
      if ($composer.shouldExecute(($changed & 3) != 2, $changed & 1)) {
         if (ComposerKt.isTraceInProgress()) {
            ComposerKt.traceEventStart(957082478, $changed, -1, "com.android.snippets.ui.DetailTopBar.<anonymous>.<anonymous>.<anonymous>.<anonymous>.<anonymous> (DetailComponents.kt:195)");
         }

         byte var14 = 0;
         int var15 = 0;
         ComposerKt.sourceInformationMarkerStart($composer, 1341605231, "CC(Column)N(modifier,verticalArrangement,horizontalAlignment,content)87@4443L61,88@4509L134:Column.kt#2w3rfo");
         Modifier var10 = (Modifier)Modifier.Companion;
         Arrangement.Vertical var11 = Arrangement.INSTANCE.getTop();
         Alignment.Horizontal var12 = Alignment.Companion.getStart();
         MeasurePolicy var16 = ColumnKt.columnMeasurePolicy(var11, var12, $composer, 14 & var14 >> 3 | 112 & var14 >> 3);
         int var20 = 112 & var14 << 3;
         int var21 = 0;
         ComposerKt.sourceInformationMarkerStart($composer, -1159599143, "CC(Layout)N(content,modifier,measurePolicy)81@3355L27,84@3521L415:Layout.kt#80mrfh");
         int var22 = Long.hashCode(ComposablesKt.getCurrentCompositeKeyHashCode($composer, 0));
         CompositionLocalMap var23 = $composer.getCurrentCompositionLocalMap();
         Modifier var24 = ComposedModifierKt.materializeModifier($composer, var10);
         Function0 var25 = ComposeUiNode.Companion.getConstructor();
         int var27 = 6 | 896 & var20 << 6;
         int var28 = 0;
         ComposerKt.sourceInformationMarkerStart($composer, -553112988, "CC(ReusableComposeNode)N(factory,update,content)410@16187L9:Composables.kt#9igjgp");
         if (!($composer.getApplier() instanceof Applier)) {
            ComposablesKt.invalidApplier();
         }

         $composer.startReusableNode();
         if ($composer.getInserting()) {
            $composer.createNode(var25);
         } else {
            $composer.useNode();
         }

         Composer var29 = Updater.constructor-impl($composer);
         int var30 = 0;
         Updater.set-impl(var29, var16, ComposeUiNode.Companion.getSetMeasurePolicy());
         Updater.set-impl(var29, var23, ComposeUiNode.Companion.getSetResolvedCompositionLocals());
         Updater.set-impl(var29, var22, ComposeUiNode.Companion.getSetCompositeKeyHash());
         Updater.reconcile-impl(var29, ComposeUiNode.Companion.getApplyOnDeactivatedNodeAssertion());
         Updater.set-impl(var29, var24, ComposeUiNode.Companion.getSetModifier());
         int var31 = 14 & var27 >> 6;
         int var33 = 0;
         ComposerKt.sourceInformationMarkerStart($composer, 2093002350, "C89@4557L9:Column.kt#2w3rfo");
         int var34 = 6 | 112 & var14 >> 6;
         ColumnScope var10000 = (ColumnScope)ColumnScopeInstance.INSTANCE;
         int var37 = 0;
         ComposerKt.sourceInformationMarkerStart($composer, 1073210229, "C204@9687L57,206@10032L94,205@9796L188,203@9626L534:DetailComponents.kt#wxf82o");
         if (!$hasSnippets) {
            $composer.startReplaceGroup(1064084138);
            $composer.endReplaceGroup();
         } else {
            $composer.startReplaceGroup(1073190078);
            ComposerKt.sourceInformation($composer, "200@9435L86,197@9203L356");
            Function2 var63 = com.android.snippets.ui.ComposableSingletons.DetailComponentsKt.INSTANCE.getLambda$-1184714643$app_debug();
            ComposerKt.sourceInformationMarkerStart($composer, 1420100654, "CC(remember):DetailComponents.kt#9igjgp");
            boolean var39 = $composer.changedInstance($view) | $composer.changed($onEdit) | $composer.changed($closeMenu);
            int var40 = 0;
            Object var41 = $composer.rememberedValue();
            int var42 = 0;
            Object var66;
            if (!var39 && var41 != Composer.Companion.getEmpty()) {
               var66 = var41;
            } else {
               Function2 var43 = var63;
               int var44 = 0;
               Function0 var10001 = DetailComponentsKt::DetailTopBar$lambda$45$lambda$44$lambda$43$lambda$42$lambda$37$lambda$36$lambda$28$lambda$27;
               var63 = var43;
               Object var45 = var10001;
               $composer.updateRememberedValue(var45);
               var66 = var45;
            }

            Function0 var46 = (Function0)var66;
            ComposerKt.sourceInformationMarkerEnd($composer);
            AndroidMenu_androidKt.DropdownMenuItem(var63, var46, (Modifier)null, com.android.snippets.ui.ComposableSingletons.DetailComponentsKt.INSTANCE.getLambda$-1277485430$app_debug(), (Function2)null, false, (MenuItemColors)null, (PaddingValues)null, (MutableInteractionSource)null, $composer, 3078, 500);
            $composer.endReplaceGroup();
         }

         Function2 var64 = (Function2)ComposableLambdaKt.rememberComposableLambda(-2008030232, true, DetailComponentsKt::DetailTopBar$lambda$45$lambda$44$lambda$43$lambda$42$lambda$37$lambda$36$lambda$29, $composer, 54);
         ComposerKt.sourceInformationMarkerStart($composer, 1420119766, "CC(remember):DetailComponents.kt#9igjgp");
         boolean var47 = $composer.changedInstance($view) | $composer.changed($onTogglePublic) | $composer.changed($closeMenu);
         int var49 = 0;
         Object var51 = $composer.rememberedValue();
         int var53 = 0;
         Object var68;
         if (!var47 && var51 != Composer.Companion.getEmpty()) {
            var68 = var51;
         } else {
            Function2 var55 = var64;
            int var57 = 0;
            Function0 var67 = DetailComponentsKt::DetailTopBar$lambda$45$lambda$44$lambda$43$lambda$42$lambda$37$lambda$36$lambda$31$lambda$30;
            var64 = var55;
            Object var59 = var67;
            $composer.updateRememberedValue(var59);
            var68 = var59;
         }

         Function0 var61 = (Function0)var68;
         ComposerKt.sourceInformationMarkerEnd($composer);
         AndroidMenu_androidKt.DropdownMenuItem(var64, var61, (Modifier)null, (Function2)ComposableLambdaKt.rememberComposableLambda(-329719227, true, DetailComponentsKt::DetailTopBar$lambda$45$lambda$44$lambda$43$lambda$42$lambda$37$lambda$36$lambda$32, $composer, 54), (Function2)null, false, (MenuItemColors)null, (PaddingValues)null, (MutableInteractionSource)null, $composer, 3078, 500);
         if (!$isPublic) {
            $composer.startReplaceGroup(1064084138);
            $composer.endReplaceGroup();
         } else {
            $composer.startReplaceGroup(1074227927);
            ComposerKt.sourceInformation($composer, "210@10310L58,212@10604L94,209@10245L491");
            var64 = (Function2)ComposableLambdaKt.rememberComposableLambda(-454688426, true, DetailComponentsKt::DetailTopBar$lambda$45$lambda$44$lambda$43$lambda$42$lambda$37$lambda$36$lambda$33, $composer, 54);
            ComposerKt.sourceInformationMarkerStart($composer, 1420138070, "CC(remember):DetailComponents.kt#9igjgp");
            var47 = $composer.changedInstance($view) | $composer.changed($onAddLinkClick) | $composer.changed($closeMenu);
            var49 = 0;
            var51 = $composer.rememberedValue();
            var53 = 0;
            if (!var47 && var51 != Composer.Companion.getEmpty()) {
               var68 = var51;
            } else {
               Function2 var56 = var64;
               int var58 = 0;
               Function0 var69 = DetailComponentsKt::DetailTopBar$lambda$45$lambda$44$lambda$43$lambda$42$lambda$37$lambda$36$lambda$35$lambda$34;
               var64 = var56;
               Object var60 = var69;
               $composer.updateRememberedValue(var60);
               var68 = var60;
            }

            var61 = (Function0)var68;
            ComposerKt.sourceInformationMarkerEnd($composer);
            AndroidMenu_androidKt.DropdownMenuItem(var64, var61, (Modifier)null, com.android.snippets.ui.ComposableSingletons.DetailComponentsKt.INSTANCE.getLambda$586898483$app_debug(), (Function2)null, false, (MenuItemColors)null, (PaddingValues)null, (MutableInteractionSource)null, $composer, 3078, 500);
            $composer.endReplaceGroup();
         }

         ComposerKt.sourceInformationMarkerEnd($composer);
         ComposerKt.sourceInformationMarkerEnd($composer);
         $composer.endNode();
         ComposerKt.sourceInformationMarkerEnd($composer);
         ComposerKt.sourceInformationMarkerEnd($composer);
         ComposerKt.sourceInformationMarkerEnd($composer);
         if (ComposerKt.isTraceInProgress()) {
            ComposerKt.traceEventEnd();
         }
      } else {
         $composer.skipToGroupEnd();
      }

      return Unit.INSTANCE;
   }

   private static final Unit DetailTopBar$lambda$45$lambda$44$lambda$43$lambda$42$lambda$41$lambda$40$lambda$39$lambda$38(View $view, Function0 $onDelete, Function0 $closeMenu) {
      $view.performHapticFeedback(16);
      $onDelete.invoke();
      $closeMenu.invoke();
      return Unit.INSTANCE;
   }

   @Composable
   @ComposableTarget(
      applier = "androidx.compose.ui.UiComposable"
   )
   private static final Unit DetailTopBar$lambda$45$lambda$44$lambda$43$lambda$42$lambda$41(View $view, Function0 $onDelete, Function0 $closeMenu, Composer $composer, int $changed) {
      ComposerKt.sourceInformation($composer, "C226@11297L408:DetailComponents.kt#wxf82o");
      if ($composer.shouldExecute(($changed & 3) != 2, $changed & 1)) {
         if (ComposerKt.isTraceInProgress()) {
            ComposerKt.traceEventStart(370939887, $changed, -1, "com.android.snippets.ui.DetailTopBar.<anonymous>.<anonymous>.<anonymous>.<anonymous>.<anonymous> (DetailComponents.kt:226)");
         }

         byte var9 = 0;
         int var10 = 0;
         ComposerKt.sourceInformationMarkerStart($composer, 1341605231, "CC(Column)N(modifier,verticalArrangement,horizontalAlignment,content)87@4443L61,88@4509L134:Column.kt#2w3rfo");
         Modifier var5 = (Modifier)Modifier.Companion;
         Arrangement.Vertical var6 = Arrangement.INSTANCE.getTop();
         Alignment.Horizontal var7 = Alignment.Companion.getStart();
         MeasurePolicy var11 = ColumnKt.columnMeasurePolicy(var6, var7, $composer, 14 & var9 >> 3 | 112 & var9 >> 3);
         int var15 = 112 & var9 << 3;
         int var16 = 0;
         ComposerKt.sourceInformationMarkerStart($composer, -1159599143, "CC(Layout)N(content,modifier,measurePolicy)81@3355L27,84@3521L415:Layout.kt#80mrfh");
         int var17 = Long.hashCode(ComposablesKt.getCurrentCompositeKeyHashCode($composer, 0));
         CompositionLocalMap var18 = $composer.getCurrentCompositionLocalMap();
         Modifier var19 = ComposedModifierKt.materializeModifier($composer, var5);
         Function0 var20 = ComposeUiNode.Companion.getConstructor();
         int var22 = 6 | 896 & var15 << 6;
         int var23 = 0;
         ComposerKt.sourceInformationMarkerStart($composer, -553112988, "CC(ReusableComposeNode)N(factory,update,content)410@16187L9:Composables.kt#9igjgp");
         if (!($composer.getApplier() instanceof Applier)) {
            ComposablesKt.invalidApplier();
         }

         $composer.startReusableNode();
         if ($composer.getInserting()) {
            $composer.createNode(var20);
         } else {
            $composer.useNode();
         }

         Composer var24 = Updater.constructor-impl($composer);
         int var25 = 0;
         Updater.set-impl(var24, var11, ComposeUiNode.Companion.getSetMeasurePolicy());
         Updater.set-impl(var24, var18, ComposeUiNode.Companion.getSetResolvedCompositionLocals());
         Updater.set-impl(var24, var17, ComposeUiNode.Companion.getSetCompositeKeyHash());
         Updater.reconcile-impl(var24, ComposeUiNode.Companion.getApplyOnDeactivatedNodeAssertion());
         Updater.set-impl(var24, var19, ComposeUiNode.Companion.getSetModifier());
         int var26 = 14 & var22 >> 6;
         int var28 = 0;
         ComposerKt.sourceInformationMarkerStart($composer, 2093002350, "C89@4557L9:Column.kt#2w3rfo");
         int var29 = 6 | 112 & var9 >> 6;
         ColumnScope var10000 = (ColumnScope)ColumnScopeInstance.INSTANCE;
         int var32 = 0;
         ComposerKt.sourceInformationMarkerStart($composer, -1658699847, "C230@11553L88,227@11338L337:DetailComponents.kt#wxf82o");
         Function2 var42 = com.android.snippets.ui.ComposableSingletons.DetailComponentsKt.INSTANCE.getLambda$1700794473$app_debug();
         ComposerKt.sourceInformationMarkerStart($composer, 777784177, "CC(remember):DetailComponents.kt#9igjgp");
         boolean var34 = $composer.changedInstance($view) | $composer.changed($onDelete) | $composer.changed($closeMenu);
         int var35 = 0;
         Object var36 = $composer.rememberedValue();
         int var37 = 0;
         Object var43;
         if (!var34 && var36 != Composer.Companion.getEmpty()) {
            var43 = var36;
         } else {
            Function2 var38 = var42;
            int var39 = 0;
            Function0 var10001 = DetailComponentsKt::DetailTopBar$lambda$45$lambda$44$lambda$43$lambda$42$lambda$41$lambda$40$lambda$39$lambda$38;
            var42 = var38;
            Object var40 = var10001;
            $composer.updateRememberedValue(var40);
            var43 = var40;
         }

         Function0 var41 = (Function0)var43;
         ComposerKt.sourceInformationMarkerEnd($composer);
         AndroidMenu_androidKt.DropdownMenuItem(var42, var41, (Modifier)null, com.android.snippets.ui.ComposableSingletons.DetailComponentsKt.INSTANCE.getLambda$-915861818$app_debug(), (Function2)null, false, (MenuItemColors)null, (PaddingValues)null, (MutableInteractionSource)null, $composer, 3078, 500);
         ComposerKt.sourceInformationMarkerEnd($composer);
         ComposerKt.sourceInformationMarkerEnd($composer);
         $composer.endNode();
         ComposerKt.sourceInformationMarkerEnd($composer);
         ComposerKt.sourceInformationMarkerEnd($composer);
         ComposerKt.sourceInformationMarkerEnd($composer);
         if (ComposerKt.isTraceInProgress()) {
            ComposerKt.traceEventEnd();
         }
      } else {
         $composer.skipToGroupEnd();
      }

      return Unit.INSTANCE;
   }

   @Composable
   @ComposableTarget(
      applier = "androidx.compose.ui.UiComposable"
   )
   private static final Unit DetailTopBar$lambda$45$lambda$44$lambda$43(View $view, Function0 $onDownload, boolean $hasSnippets, Function0 $onShare, Function0 $onToggleFavorite, boolean $isFavorite, Function0 $onEdit, Function0 $onTogglePublic, boolean $isPublic, Function0 $onAddLinkClick, boolean $hasLocationLink, Function0 $onDelete, ColumnScope $this$SplitButton, Function0 closeMenu, Composer $composer, int $changed) {
      Intrinsics.checkNotNullParameter($this$SplitButton, "$this$SplitButton");
      Intrinsics.checkNotNullParameter(closeMenu, "closeMenu");
      ComposerKt.sourceInformation($composer, "CN(closeMenu)156@6597L5156:DetailComponents.kt#wxf82o");
      int $dirty = $changed;
      if (($changed & 48) == 0) {
         $dirty = $changed | ($composer.changedInstance(closeMenu) ? 32 : 16);
      }

      if ($composer.shouldExecute(($dirty & 145) != 144, $dirty & 1)) {
         if (ComposerKt.isTraceInProgress()) {
            ComposerKt.traceEventStart(524482022, $dirty, -1, "com.android.snippets.ui.DetailTopBar.<anonymous>.<anonymous>.<anonymous> (DetailComponents.kt:154)");
         }

         int var18 = 12;
         int var19 = 0;
         RoundedCornerShape menuGroupShape = RoundedCornerShapeKt.RoundedCornerShape-0680j_4(Dp.constructor-impl((float)var18));
         var19 = 2;
         int var20 = 0;
         Arrangement.Vertical var55 = (Arrangement.Vertical)Arrangement.INSTANCE.spacedBy-0680j_4(Dp.constructor-impl((float)var19));
         int var22 = 48;
         int var23 = 0;
         ComposerKt.sourceInformationMarkerStart($composer, 1341605231, "CC(Column)N(modifier,verticalArrangement,horizontalAlignment,content)87@4443L61,88@4509L134:Column.kt#2w3rfo");
         Modifier var53 = (Modifier)Modifier.Companion;
         Alignment.Horizontal var56 = Alignment.Companion.getStart();
         MeasurePolicy var24 = ColumnKt.columnMeasurePolicy(var55, var56, $composer, 14 & var22 >> 3 | 112 & var22 >> 3);
         int var28 = 112 & var22 << 3;
         int var29 = 0;
         ComposerKt.sourceInformationMarkerStart($composer, -1159599143, "CC(Layout)N(content,modifier,measurePolicy)81@3355L27,84@3521L415:Layout.kt#80mrfh");
         int var30 = Long.hashCode(ComposablesKt.getCurrentCompositeKeyHashCode($composer, 0));
         CompositionLocalMap var31 = $composer.getCurrentCompositionLocalMap();
         Modifier var32 = ComposedModifierKt.materializeModifier($composer, var53);
         Function0 var33 = ComposeUiNode.Companion.getConstructor();
         int var35 = 6 | 896 & var28 << 6;
         int var36 = 0;
         ComposerKt.sourceInformationMarkerStart($composer, -553112988, "CC(ReusableComposeNode)N(factory,update,content)410@16187L9:Composables.kt#9igjgp");
         if (!($composer.getApplier() instanceof Applier)) {
            ComposablesKt.invalidApplier();
         }

         $composer.startReusableNode();
         if ($composer.getInserting()) {
            $composer.createNode(var33);
         } else {
            $composer.useNode();
         }

         Composer var37 = Updater.constructor-impl($composer);
         int var38 = 0;
         Updater.set-impl(var37, var24, ComposeUiNode.Companion.getSetMeasurePolicy());
         Updater.set-impl(var37, var31, ComposeUiNode.Companion.getSetResolvedCompositionLocals());
         Updater.set-impl(var37, var30, ComposeUiNode.Companion.getSetCompositeKeyHash());
         Updater.reconcile-impl(var37, ComposeUiNode.Companion.getApplyOnDeactivatedNodeAssertion());
         Updater.set-impl(var37, var32, ComposeUiNode.Companion.getSetModifier());
         int var39 = 14 & var35 >> 6;
         int var41 = 0;
         ComposerKt.sourceInformationMarkerStart($composer, 2093002350, "C89@4557L9:Column.kt#2w3rfo");
         int var42 = 6 | 112 & var22 >> 6;
         ColumnScope var10000 = (ColumnScope)ColumnScopeInstance.INSTANCE;
         int var45 = 0;
         ComposerKt.sourceInformationMarkerStart($composer, 1137732298, "C160@6853L11,161@6944L11,164@7114L1484,158@6742L1856,190@8816L11,191@8907L11,194@9077L1749,188@8705L2121,221@11006L11,222@11097L11,225@11267L464,219@10895L836:DetailComponents.kt#wxf82o");
         long var46 = MaterialTheme.INSTANCE.getColorScheme($composer, MaterialTheme.$stable).getSurfaceContainerHigh-0d7_KjU();
         long var48 = MaterialTheme.INSTANCE.getColorScheme($composer, MaterialTheme.$stable).getOnSurface-0d7_KjU();
         int var50 = 4;
         int var51 = 0;
         float var52 = Dp.constructor-impl((float)var50);
         SurfaceKt.Surface-T9BRK9s(ClipKt.clip((Modifier)Modifier.Companion, (Shape)menuGroupShape), (Shape)menuGroupShape, var46, var48, 0.0F, var52, (BorderStroke)null, (Function2)ComposableLambdaKt.rememberComposableLambda(-916838601, true, DetailComponentsKt::DetailTopBar$lambda$45$lambda$44$lambda$43$lambda$42$lambda$26, $composer, 54), $composer, 12779520, 80);
         var46 = MaterialTheme.INSTANCE.getColorScheme($composer, MaterialTheme.$stable).getSurfaceContainerHigh-0d7_KjU();
         var48 = MaterialTheme.INSTANCE.getColorScheme($composer, MaterialTheme.$stable).getOnSurface-0d7_KjU();
         var50 = 4;
         var51 = 0;
         var52 = Dp.constructor-impl((float)var50);
         SurfaceKt.Surface-T9BRK9s(ClipKt.clip((Modifier)Modifier.Companion, (Shape)menuGroupShape), (Shape)menuGroupShape, var46, var48, 0.0F, var52, (BorderStroke)null, (Function2)ComposableLambdaKt.rememberComposableLambda(957082478, true, DetailComponentsKt::DetailTopBar$lambda$45$lambda$44$lambda$43$lambda$42$lambda$37, $composer, 54), $composer, 12779520, 80);
         var46 = MaterialTheme.INSTANCE.getColorScheme($composer, MaterialTheme.$stable).getSurfaceContainerHigh-0d7_KjU();
         var48 = MaterialTheme.INSTANCE.getColorScheme($composer, MaterialTheme.$stable).getOnSurface-0d7_KjU();
         var50 = 4;
         var51 = 0;
         var52 = Dp.constructor-impl((float)var50);
         SurfaceKt.Surface-T9BRK9s(ClipKt.clip((Modifier)Modifier.Companion, (Shape)menuGroupShape), (Shape)menuGroupShape, var46, var48, 0.0F, var52, (BorderStroke)null, (Function2)ComposableLambdaKt.rememberComposableLambda(370939887, true, DetailComponentsKt::DetailTopBar$lambda$45$lambda$44$lambda$43$lambda$42$lambda$41, $composer, 54), $composer, 12779520, 80);
         ComposerKt.sourceInformationMarkerEnd($composer);
         ComposerKt.sourceInformationMarkerEnd($composer);
         $composer.endNode();
         ComposerKt.sourceInformationMarkerEnd($composer);
         ComposerKt.sourceInformationMarkerEnd($composer);
         ComposerKt.sourceInformationMarkerEnd($composer);
         if (ComposerKt.isTraceInProgress()) {
            ComposerKt.traceEventEnd();
         }
      } else {
         $composer.skipToGroupEnd();
      }

      return Unit.INSTANCE;
   }

   @Composable
   @ComposableTarget(
      applier = "androidx.compose.ui.UiComposable"
   )
   private static final Unit DetailTopBar$lambda$45(AnimatedVisibilityScope $animatedVisibilityScope, View $view, Function0 $onAdd, Function0 $onBack, boolean $isSpinning, Function0 $onDownload, boolean $hasSnippets, Function0 $onShare, Function0 $onToggleFavorite, boolean $isFavorite, Function0 $onEdit, Function0 $onTogglePublic, boolean $isPublic, Function0 $onAddLinkClick, boolean $hasLocationLink, Function0 $onDelete, Composer $composer, int $changed) {
      ComposerKt.sourceInformation($composer, "C112@4475L7320:DetailComponents.kt#wxf82o");
      if ($composer.shouldExecute(($changed & 3) != 2, $changed & 1)) {
         if (ComposerKt.isTraceInProgress()) {
            ComposerKt.traceEventStart(1897722738, $changed, -1, "com.android.snippets.ui.DetailTopBar.<anonymous> (DetailComponents.kt:112)");
         }

         Modifier var10000 = WindowInsetsPadding_androidKt.statusBarsPadding(SizeKt.fillMaxWidth$default((Modifier)Modifier.Companion, 0.0F, 1, (Object)null));
         Modifier var130;
         if ($animatedVisibilityScope != null) {
            $composer.startReplaceGroup(403784040);
            ComposerKt.sourceInformation($composer, "*119@4780L12,121@5011L11,122@5188L11");
            Modifier var86 = var10000;
            int var22 = 0;
            MotionScheme var23 = MaterialTheme.INSTANCE.getMotionScheme($composer, MaterialTheme.$stable);
            AnimatedVisibilityScope var125 = $animatedVisibilityScope;
            var130 = (Modifier)Modifier.Companion;
            EnterTransition var10002 = EnterExitTransitionKt.fadeIn$default(var23.fastEffectsSpec(), 0.0F, 2, (Object)null);
            FiniteAnimationSpec var10003 = var23.fastSpatialSpec();
            ComposerKt.sourceInformationMarkerStart($composer, -1371773441, "CC(remember):DetailComponents.kt#9igjgp");
            boolean var25 = false;
            int var26 = 0;
            Object var40_1 = $composer.rememberedValue();
            int var28 = 0;
            Object var10004;
            if (var40_1 == Composer.Companion.getEmpty()) {
               FiniteAnimationSpec var29 = var10003;
               EnterTransition var30 = var10002;
               Modifier var31 = var130;
               int var33 = 0;
               Function1 var33_2 = DetailComponentsKt::DetailTopBar$lambda$45$lambda$13$lambda$10$lambda$9;
               var125 = $animatedVisibilityScope;
               var130 = var31;
               var10002 = var30;
               var10003 = var29;
               $composer.updateRememberedValue(var33_2);
               var10004 = var33_2;
            } else {
               var10004 = var40_1;
            }

            Function1 var35 = (Function1)var10004;
            ComposerKt.sourceInformationMarkerEnd($composer);
            var10002 = var10002.plus(EnterExitTransitionKt.slideInVertically(var10003, var35));
            ExitTransition var134 = EnterExitTransitionKt.fadeOut$default(var23.fastEffectsSpec(), 0.0F, 2, (Object)null);
            FiniteAnimationSpec var135 = var23.fastSpatialSpec();
            ComposerKt.sourceInformationMarkerStart($composer, -1371767777, "CC(remember):DetailComponents.kt#9igjgp");
            var25 = false;
            var26 = 0;
            var40_1 = $composer.rememberedValue();
            var28 = 0;
            Object var10005;
            if (var40_1 == Composer.Companion.getEmpty()) {
               FiniteAnimationSpec var33_2 = var135;
               ExitTransition var107 = var134;
               EnterTransition var109 = var10002;
               Modifier var111 = var130;
               AnimatedVisibilityScope var32 = var125;
               int var114 = 0;
               Function1 var33_6 = DetailComponentsKt::DetailTopBar$lambda$45$lambda$13$lambda$12$lambda$11;
               var125 = var32;
               var130 = var111;
               var10002 = var109;
               var134 = var107;
               var135 = var33_2;
               $composer.updateRememberedValue(var33_6);
               var10005 = var33_6;
            } else {
               var10005 = var40_1;
            }

            var35 = (Function1)var10005;
            ComposerKt.sourceInformationMarkerEnd($composer);
            var130 = AnimatedVisibilityScope.animateEnterExit$default(var125, var130, var10002, var134.plus(EnterExitTransitionKt.slideOutVertically(var135, var35)), (String)null, 4, (Object)null);
            var10000 = var86;
            Modifier var19 = var130;
            $composer.endReplaceGroup();
            var130 = var19;
         } else {
            $composer.startReplaceGroup(-1649523398);
            $composer.endReplaceGroup();
            var130 = (Modifier)Modifier.Companion;
         }

         Modifier var18 = var10000.then(var130);
         int var20 = 16;
         int var21 = 0;
         float var88 = Dp.constructor-impl((float)var20);
         var21 = 24;
         int var94 = 0;
         float var90 = Dp.constructor-impl((float)var21);
         var94 = 16;
         int var98 = 0;
         float var93 = Dp.constructor-impl((float)var94);
         var98 = 20;
         int var24 = 0;
         float var96 = Dp.constructor-impl((float)var98);
         var18 = PaddingKt.padding-qDBjuR0(var18, var88, var93, var90, var96);
         Arrangement.Horizontal var89 = (Arrangement.Horizontal)Arrangement.INSTANCE.getSpaceBetween();
         Alignment.Vertical var91 = Alignment.Companion.getCenterVertically();
         short var97 = 432;
         var98 = 0;
         ComposerKt.sourceInformationMarkerStart($composer, 844473419, "CC(Row)N(modifier,horizontalArrangement,verticalAlignment,content)99@5125L58,100@5188L131:Row.kt#2w3rfo");
         MeasurePolicy var101 = RowKt.rowMeasurePolicy(var89, var91, $composer, 14 & var97 >> 3 | 112 & var97 >> 3);
         int var106 = 112 & var97 << 3;
         int var108 = 0;
         ComposerKt.sourceInformationMarkerStart($composer, -1159599143, "CC(Layout)N(content,modifier,measurePolicy)81@3355L27,84@3521L415:Layout.kt#80mrfh");
         int var110 = Long.hashCode(ComposablesKt.getCurrentCompositeKeyHashCode($composer, 0));
         CompositionLocalMap var112 = $composer.getCurrentCompositionLocalMap();
         Modifier var113 = ComposedModifierKt.materializeModifier($composer, var18);
         Function0 var115 = ComposeUiNode.Companion.getConstructor();
         int var118 = 6 | 896 & var106 << 6;
         int var119 = 0;
         ComposerKt.sourceInformationMarkerStart($composer, -553112988, "CC(ReusableComposeNode)N(factory,update,content)410@16187L9:Composables.kt#9igjgp");
         if (!($composer.getApplier() instanceof Applier)) {
            ComposablesKt.invalidApplier();
         }

         $composer.startReusableNode();
         if ($composer.getInserting()) {
            $composer.createNode(var115);
         } else {
            $composer.useNode();
         }

         Composer var37 = Updater.constructor-impl($composer);
         int var38 = 0;
         Updater.set-impl(var37, var101, ComposeUiNode.Companion.getSetMeasurePolicy());
         Updater.set-impl(var37, var112, ComposeUiNode.Companion.getSetResolvedCompositionLocals());
         Updater.set-impl(var37, var110, ComposeUiNode.Companion.getSetCompositeKeyHash());
         Updater.reconcile-impl(var37, ComposeUiNode.Companion.getApplyOnDeactivatedNodeAssertion());
         Updater.set-impl(var37, var113, ComposeUiNode.Companion.getSetModifier());
         int var39 = 14 & var118 >> 6;
         int var41 = 0;
         ComposerKt.sourceInformationMarkerStart($composer, 1456264949, "C101@5233L9:Row.kt#2w3rfo");
         int var42 = 6 | 112 & var97 >> 6;
         RowScope var126 = (RowScope)RowScopeInstance.INSTANCE;
         int var45 = 0;
         ComposerKt.sourceInformationMarkerStart($composer, -1368121818, "C131@5538L638,149@6331L127,153@6494L5277,146@6190L5595:DetailComponents.kt#wxf82o");
         Alignment.Vertical var46 = Alignment.Companion.getCenterVertically();
         int var48 = 384;
         int var49 = 0;
         ComposerKt.sourceInformationMarkerStart($composer, 844473419, "CC(Row)N(modifier,horizontalArrangement,verticalAlignment,content)99@5125L58,100@5188L131:Row.kt#2w3rfo");
         Modifier var50 = (Modifier)Modifier.Companion;
         Arrangement.Horizontal var51 = Arrangement.INSTANCE.getStart();
         MeasurePolicy var52 = RowKt.rowMeasurePolicy(var51, var46, $composer, 14 & var48 >> 3 | 112 & var48 >> 3);
         int var56 = 112 & var48 << 3;
         int var57 = 0;
         ComposerKt.sourceInformationMarkerStart($composer, -1159599143, "CC(Layout)N(content,modifier,measurePolicy)81@3355L27,84@3521L415:Layout.kt#80mrfh");
         int var58 = Long.hashCode(ComposablesKt.getCurrentCompositeKeyHashCode($composer, 0));
         CompositionLocalMap var59 = $composer.getCurrentCompositionLocalMap();
         Modifier var60 = ComposedModifierKt.materializeModifier($composer, var50);
         Function0 var61 = ComposeUiNode.Companion.getConstructor();
         int var63 = 6 | 896 & var56 << 6;
         int var64 = 0;
         ComposerKt.sourceInformationMarkerStart($composer, -553112988, "CC(ReusableComposeNode)N(factory,update,content)410@16187L9:Composables.kt#9igjgp");
         if (!($composer.getApplier() instanceof Applier)) {
            ComposablesKt.invalidApplier();
         }

         $composer.startReusableNode();
         if ($composer.getInserting()) {
            $composer.createNode(var61);
         } else {
            $composer.useNode();
         }

         Composer var65 = Updater.constructor-impl($composer);
         int var66 = 0;
         Updater.set-impl(var65, var52, ComposeUiNode.Companion.getSetMeasurePolicy());
         Updater.set-impl(var65, var59, ComposeUiNode.Companion.getSetResolvedCompositionLocals());
         Updater.set-impl(var65, var58, ComposeUiNode.Companion.getSetCompositeKeyHash());
         Updater.reconcile-impl(var65, ComposeUiNode.Companion.getApplyOnDeactivatedNodeAssertion());
         Updater.set-impl(var65, var60, ComposeUiNode.Companion.getSetModifier());
         int var67 = 14 & var63 >> 6;
         int var69 = 0;
         ComposerKt.sourceInformationMarkerStart($composer, 1456264949, "C101@5233L9:Row.kt#2w3rfo");
         int var70 = 6 | 112 & var48 >> 6;
         var126 = (RowScope)RowScopeInstance.INSTANCE;
         int var73 = 0;
         ComposerKt.sourceInformationMarkerStart($composer, -1356488865, "C137@5870L11,138@5951L11,132@5608L554:DetailComponents.kt#wxf82o");
         ImageVector var74 = ArrowBackKt.getArrowBack(Filled.INSTANCE);
         long var75 = MaterialTheme.INSTANCE.getColorScheme($composer, MaterialTheme.$stable).getSecondaryContainer-0d7_KjU();
         long var77 = MaterialTheme.INSTANCE.getColorScheme($composer, MaterialTheme.$stable).getOnSecondaryContainer-0d7_KjU();
         GenericShape var79 = CookieShape;
         int var80 = 56;
         int var81 = 0;
         float var82 = Dp.constructor-impl((float)var80);
         AnimatedCookieButtonKt.AnimatedCookieButton-82CdiHg($onBack, var74, (Modifier)null, "Back", "Back", var75, var77, var82, (Shape)var79, $isSpinning, false, false, true, 0.0F, $composer, 113273856, 384, 11268);
         ComposerKt.sourceInformationMarkerEnd($composer);
         ComposerKt.sourceInformationMarkerEnd($composer);
         $composer.endNode();
         ComposerKt.sourceInformationMarkerEnd($composer);
         ComposerKt.sourceInformationMarkerEnd($composer);
         ComposerKt.sourceInformationMarkerEnd($composer);
         ImageVector var128 = AddKt.getAdd(Icons.INSTANCE.getDefault());
         String var131 = "Add snippets";
         ComposerKt.sourceInformationMarkerStart($composer, -1152492363, "CC(remember):DetailComponents.kt#9igjgp");
         boolean var120 = $composer.changedInstance($view) | $composer.changed($onAdd);
         int var47 = 0;
         Object var121 = $composer.rememberedValue();
         var49 = 0;
         Object var133;
         if (!var120 && var121 != Composer.Companion.getEmpty()) {
            var133 = var121;
         } else {
            String var83 = "Add snippets";
            ImageVector var84 = var128;
            int var124 = 0;
            Function0 var53_3 = DetailComponentsKt::DetailTopBar$lambda$45$lambda$44$lambda$16$lambda$15;
            var128 = var84;
            var131 = var83;
            $composer.updateRememberedValue(var53_3);
            var133 = var53_3;
         }

         Function0 var123 = (Function0)var133;
         ComposerKt.sourceInformationMarkerEnd($composer);
         SplitButtonKt.SplitButton(var128, var131, var123, (Function4)ComposableLambdaKt.rememberComposableLambda(524482022, true, DetailComponentsKt::DetailTopBar$lambda$45$lambda$44$lambda$43, $composer, 54), (Modifier)null, $composer, 3120, 16);
         ComposerKt.sourceInformationMarkerEnd($composer);
         ComposerKt.sourceInformationMarkerEnd($composer);
         $composer.endNode();
         ComposerKt.sourceInformationMarkerEnd($composer);
         ComposerKt.sourceInformationMarkerEnd($composer);
         ComposerKt.sourceInformationMarkerEnd($composer);
         if (ComposerKt.isTraceInProgress()) {
            ComposerKt.traceEventEnd();
         }
      } else {
         $composer.skipToGroupEnd();
      }

      return Unit.INSTANCE;
   }

   private static final Unit DetailTopBar$lambda$46(Photo $photo, SnippetsViewModel $viewModel, Function0 $onBack, boolean $isSpinning, boolean $isScrolled, Function0 $onPhotoThumbnailClick, boolean $hasSnippets, Function0 $onAdd, Function0 $onDownload, Function0 $onEdit, Function0 $onShare, Function0 $onDelete, boolean $isFavorite, Function0 $onToggleFavorite, boolean $isPublic, Function0 $onTogglePublic, boolean $hasLocationLink, Function0 $onAddLinkClick, AnimatedVisibilityScope $animatedVisibilityScope, int $$changed, int $$changed1, int $$default, Composer $composer, int $force) {
      DetailTopBar($photo, $viewModel, $onBack, $isSpinning, $isScrolled, $onPhotoThumbnailClick, $hasSnippets, $onAdd, $onDownload, $onEdit, $onShare, $onDelete, $isFavorite, $onToggleFavorite, $isPublic, $onTogglePublic, $hasLocationLink, $onAddLinkClick, $animatedVisibilityScope, $composer, RecomposeScopeImplKt.updateChangedFlags($$changed | 1), RecomposeScopeImplKt.updateChangedFlags($$changed1), $$default);
      return Unit.INSTANCE;
   }

   private static final float SwipePrompt$lambda$47(State<Dp> $bounceOffset$delegate) {
      Object var2 = null;
      KProperty var3 = null;
      int var4 = 0;
      return ((Dp)$bounceOffset$delegate.getValue()).unbox-impl();
   }

   private static final Unit SwipePrompt$lambda$49$lambda$48(float $alphaValue, GraphicsLayerScope $this$graphicsLayer) {
      Intrinsics.checkNotNullParameter($this$graphicsLayer, "$this$graphicsLayer");
      $this$graphicsLayer.setAlpha(RangesKt.coerceIn($alphaValue, 0.0F, 1.0F));
      return Unit.INSTANCE;
   }

   private static final Unit SwipePrompt$lambda$51(Modifier $modifier, float $alphaValue, int $$changed, int $$default, Composer $composer, int $force) {
      SwipePrompt($modifier, $alphaValue, $composer, RecomposeScopeImplKt.updateChangedFlags($$changed | 1), $$default);
      return Unit.INSTANCE;
   }

   @Composable
   @ComposableTarget(
      applier = "androidx.compose.ui.UiComposable"
   )
   private static final Unit CloudSnippetItem$lambda$57$lambda$56(int $totalCount, int $personality, SnippetStyle $forcedStyle, String $text, long $snippetColor, Composer $composer, int $changed) {
      ComposerKt.sourceInformation($composer, "C:DetailComponents.kt#wxf82o");
      if ($composer.shouldExecute(($changed & 3) != 2, $changed & 1)) {
         if (ComposerKt.isTraceInProgress()) {
            ComposerKt.traceEventStart(1570753851, $changed, -1, "com.android.snippets.ui.CloudSnippetItem.<anonymous>.<anonymous> (DetailComponents.kt:395)");
         }

         float scalingFactor = DistributionMath.INSTANCE.getGridScalingFactor($totalCount);
         switch ($personality) {
            case 0:
               $composer.startReplaceGroup(659473455);
               ComposerKt.sourceInformation($composer, "401@19074L10,401@18972L143,401@19147L10,399@18896L508");
               SnippetStyle var46 = $forcedStyle;
               if ($forcedStyle == null) {
                  var46 = SnippetStyle.Default;
               }

               TextStyle var16 = TextStyle.copy-p1EtxEg$default(getSnippetTextStyle(var46, MaterialTheme.INSTANCE.getTypography($composer, MaterialTheme.$stable).getDisplayMedium(), true, $composer, 384, 0), 0L, TextUnitKt.getSp(TextUnit.getValue-impl(MaterialTheme.INSTANCE.getTypography($composer, MaterialTheme.$stable).getDisplayMedium().getFontSize-XSAIIZE()) * scalingFactor), (FontWeight)null, (FontStyle)null, (FontSynthesis)null, (FontFamily)null, (String)null, 0L, (BaselineShift)null, (TextGeometricTransform)null, (LocaleList)null, 0L, (TextDecoration)null, (Shadow)null, (DrawStyle)null, 0, 0, 0L, (TextIndent)null, (PlatformTextStyle)null, (LineHeightStyle)null, 0, 0, (TextMotion)null, 16777213, (Object)null);
               Modifier var47 = (Modifier)Modifier.Companion;
               float var36 = (float)32 * scalingFactor;
               int var12 = 0;
               float var51 = Dp.constructor-impl(var36);
               var36 = (float)16 * scalingFactor;
               var12 = 0;
               Modifier var28 = PaddingKt.padding-VpY3zN4(var47, var51, Dp.constructor-impl(var36));
               TextKt.Text-Nvy7gAk($text, var28, $snippetColor, (TextAutoSize)null, 0L, (FontStyle)null, (FontWeight)null, (FontFamily)null, 0L, (TextDecoration)null, (TextAlign)null, 0L, 0, false, 0, 0, (Function1)null, var16, $composer, 0, 0, 131064);
               $composer.endReplaceGroup();
               break;
            case 1:
               $composer.startReplaceGroup(660058735);
               ComposerKt.sourceInformation($composer, "410@19790L10,410@19688L144,410@19864L10,407@19484L510");
               Modifier var44 = (Modifier)Modifier.Companion;
               float var25 = (float)24 * scalingFactor;
               int var34 = 0;
               float var50 = Dp.constructor-impl(var25);
               var25 = (float)12 * scalingFactor;
               var34 = 0;
               Modifier var15 = PaddingKt.padding-VpY3zN4(var44, var50, Dp.constructor-impl(var25));
               SnippetStyle var45 = $forcedStyle;
               if ($forcedStyle == null) {
                  var45 = SnippetStyle.Default;
               }

               TextStyle var27 = TextStyle.copy-p1EtxEg$default(getSnippetTextStyle(var45, MaterialTheme.INSTANCE.getTypography($composer, MaterialTheme.$stable).getHeadlineMedium(), true, $composer, 384, 0), 0L, TextUnitKt.getSp(TextUnit.getValue-impl(MaterialTheme.INSTANCE.getTypography($composer, MaterialTheme.$stable).getHeadlineMedium().getFontSize-XSAIIZE()) * scalingFactor), (FontWeight)null, (FontStyle)null, (FontSynthesis)null, (FontFamily)null, (String)null, 0L, (BaselineShift)null, (TextGeometricTransform)null, (LocaleList)null, 0L, (TextDecoration)null, (Shadow)null, (DrawStyle)null, 0, 0, 0L, (TextIndent)null, (PlatformTextStyle)null, (LineHeightStyle)null, 0, 0, (TextMotion)null, 16777213, (Object)null);
               TextKt.Text-Nvy7gAk($text, var15, $snippetColor, (TextAutoSize)null, 0L, (FontStyle)null, (FontWeight)null, (FontFamily)null, 0L, (TextDecoration)null, (TextAlign)null, 0L, 0, false, 0, 0, (Function1)null, var27, $composer, 0, 0, 131064);
               $composer.endReplaceGroup();
               break;
            case 2:
               $composer.startReplaceGroup(660644108);
               ComposerKt.sourceInformation($composer, "418@20385L10,418@20283L143,418@20458L10,415@20079L508");
               Modifier var42 = (Modifier)Modifier.Companion;
               float var22 = (float)20 * scalingFactor;
               int var32 = 0;
               float var49 = Dp.constructor-impl(var22);
               var22 = (float)10 * scalingFactor;
               var32 = 0;
               Modifier var14 = PaddingKt.padding-VpY3zN4(var42, var49, Dp.constructor-impl(var22));
               SnippetStyle var43 = $forcedStyle;
               if ($forcedStyle == null) {
                  var43 = SnippetStyle.Default;
               }

               TextStyle var24 = TextStyle.copy-p1EtxEg$default(getSnippetTextStyle(var43, MaterialTheme.INSTANCE.getTypography($composer, MaterialTheme.$stable).getHeadlineSmall(), true, $composer, 384, 0), 0L, TextUnitKt.getSp(TextUnit.getValue-impl(MaterialTheme.INSTANCE.getTypography($composer, MaterialTheme.$stable).getHeadlineSmall().getFontSize-XSAIIZE()) * scalingFactor), (FontWeight)null, (FontStyle)null, (FontSynthesis)null, (FontFamily)null, (String)null, 0L, (BaselineShift)null, (TextGeometricTransform)null, (LocaleList)null, 0L, (TextDecoration)null, (Shadow)null, (DrawStyle)null, 0, 0, 0L, (TextIndent)null, (PlatformTextStyle)null, (LineHeightStyle)null, 0, 0, (TextMotion)null, 16777213, (Object)null);
               TextKt.Text-Nvy7gAk($text, var14, $snippetColor, (TextAutoSize)null, 0L, (FontStyle)null, (FontWeight)null, (FontFamily)null, 0L, (TextDecoration)null, (TextAlign)null, 0L, 0, false, 0, 0, (Function1)null, var24, $composer, 0, 0, 131064);
               $composer.endReplaceGroup();
               break;
            case 3:
               $composer.startReplaceGroup(661231930);
               ComposerKt.sourceInformation($composer, "426@20970L10,426@20868L140,426@21040L10,423@20665L501");
               Modifier var40 = (Modifier)Modifier.Companion;
               float var19 = (float)16 * scalingFactor;
               int var30 = 0;
               float var48 = Dp.constructor-impl(var19);
               var19 = (float)8 * scalingFactor;
               var30 = 0;
               Modifier var13 = PaddingKt.padding-VpY3zN4(var40, var48, Dp.constructor-impl(var19));
               SnippetStyle var41 = $forcedStyle;
               if ($forcedStyle == null) {
                  var41 = SnippetStyle.Default;
               }

               TextStyle var21 = TextStyle.copy-p1EtxEg$default(getSnippetTextStyle(var41, MaterialTheme.INSTANCE.getTypography($composer, MaterialTheme.$stable).getTitleLarge(), true, $composer, 384, 0), 0L, TextUnitKt.getSp(TextUnit.getValue-impl(MaterialTheme.INSTANCE.getTypography($composer, MaterialTheme.$stable).getTitleLarge().getFontSize-XSAIIZE()) * scalingFactor), (FontWeight)null, (FontStyle)null, (FontSynthesis)null, (FontFamily)null, (String)null, 0L, (BaselineShift)null, (TextGeometricTransform)null, (LocaleList)null, 0L, (TextDecoration)null, (Shadow)null, (DrawStyle)null, 0, 0, 0L, (TextIndent)null, (PlatformTextStyle)null, (LineHeightStyle)null, 0, 0, (TextMotion)null, 16777213, (Object)null);
               TextKt.Text-Nvy7gAk($text, var13, $snippetColor, (TextAutoSize)null, 0L, (FontStyle)null, (FontWeight)null, (FontFamily)null, 0L, (TextDecoration)null, (TextAlign)null, 0L, 0, false, 0, 0, (Function1)null, var21, $composer, 0, 0, 131064);
               $composer.endReplaceGroup();
               break;
            case 4:
               $composer.startReplaceGroup(661806205);
               ComposerKt.sourceInformation($composer, "434@21546L10,434@21444L140,434@21616L10,431@21241L501");
               Modifier var10000 = (Modifier)Modifier.Companion;
               float var10 = (float)12 * scalingFactor;
               int var11 = 0;
               float var10001 = Dp.constructor-impl(var10);
               var10 = (float)6 * scalingFactor;
               var11 = 0;
               Modifier var9 = PaddingKt.padding-VpY3zN4(var10000, var10001, Dp.constructor-impl(var10));
               SnippetStyle var39 = $forcedStyle;
               if ($forcedStyle == null) {
                  var39 = SnippetStyle.Default;
               }

               TextStyle var18 = TextStyle.copy-p1EtxEg$default(getSnippetTextStyle(var39, MaterialTheme.INSTANCE.getTypography($composer, MaterialTheme.$stable).getLabelLarge(), true, $composer, 384, 0), 0L, TextUnitKt.getSp(TextUnit.getValue-impl(MaterialTheme.INSTANCE.getTypography($composer, MaterialTheme.$stable).getLabelLarge().getFontSize-XSAIIZE()) * scalingFactor), (FontWeight)null, (FontStyle)null, (FontSynthesis)null, (FontFamily)null, (String)null, 0L, (BaselineShift)null, (TextGeometricTransform)null, (LocaleList)null, 0L, (TextDecoration)null, (Shadow)null, (DrawStyle)null, 0, 0, 0L, (TextIndent)null, (PlatformTextStyle)null, (LineHeightStyle)null, 0, 0, (TextMotion)null, 16777213, (Object)null);
               TextKt.Text-Nvy7gAk($text, var9, $snippetColor, (TextAutoSize)null, 0L, (FontStyle)null, (FontWeight)null, (FontFamily)null, 0L, (TextDecoration)null, (TextAlign)null, 0L, 0, false, 0, 0, (Function1)null, var18, $composer, 0, 0, 131064);
               $composer.endReplaceGroup();
               break;
            default:
               $composer.startReplaceGroup(640751687);
               $composer.endReplaceGroup();
         }

         if (ComposerKt.isTraceInProgress()) {
            ComposerKt.traceEventEnd();
         }
      } else {
         $composer.skipToGroupEnd();
      }

      return Unit.INSTANCE;
   }

   private static final Unit CloudSnippetItem$lambda$58(String $text, int $index, int $totalCount, List $photoColors, Integer $forcedColor, SnippetStyle $forcedStyle, int $$changed, int $$default, Composer $composer, int $force) {
      CloudSnippetItem($text, $index, $totalCount, $photoColors, $forcedColor, $forcedStyle, $composer, RecomposeScopeImplKt.updateChangedFlags($$changed | 1), $$default);
      return Unit.INSTANCE;
   }

   private static final Unit ActionIcon_iJQMabo$lambda$59(ImageVector $icon, long $tint, Function0 $onClick, int $$changed, Composer $composer, int $force) {
      ActionIcon-iJQMabo($icon, $tint, $onClick, $composer, RecomposeScopeImplKt.updateChangedFlags($$changed | 1));
      return Unit.INSTANCE;
   }

   private static final Unit HeaderActionButton_iJQMabo$lambda$61$lambda$60(View $view, Function0 $onClick) {
      $view.performHapticFeedback(16);
      $onClick.invoke();
      return Unit.INSTANCE;
   }

   @Composable
   @ComposableTarget(
      applier = "androidx.compose.ui.UiComposable"
   )
   private static final Unit HeaderActionButton_iJQMabo$lambda$63(ImageVector $icon, long $color, Composer $composer, int $changed) {
      ComposerKt.sourceInformation($composer, "C464@22444L143:DetailComponents.kt#wxf82o");
      if ($composer.shouldExecute(($changed & 3) != 2, $changed & 1)) {
         if (ComposerKt.isTraceInProgress()) {
            ComposerKt.traceEventStart(1869665858, $changed, -1, "com.android.snippets.ui.HeaderActionButton.<anonymous> (DetailComponents.kt:464)");
         }

         Alignment var6 = Alignment.Companion.getCenter();
         int var9 = 48;
         int var10 = 0;
         ComposerKt.sourceInformationMarkerStart($composer, 1042775818, "CC(Box)N(modifier,contentAlignment,propagateMinConstraints,content)71@3424L131:Box.kt#2w3rfo");
         Modifier var5 = (Modifier)Modifier.Companion;
         boolean var7 = false;
         MeasurePolicy var11 = BoxKt.maybeCachedBoxMeasurePolicy(var6, var7);
         int var15 = 112 & var9 << 3;
         int var16 = 0;
         ComposerKt.sourceInformationMarkerStart($composer, -1159599143, "CC(Layout)N(content,modifier,measurePolicy)81@3355L27,84@3521L415:Layout.kt#80mrfh");
         int var17 = Long.hashCode(ComposablesKt.getCurrentCompositeKeyHashCode($composer, 0));
         CompositionLocalMap var18 = $composer.getCurrentCompositionLocalMap();
         Modifier var19 = ComposedModifierKt.materializeModifier($composer, var5);
         Function0 var20 = ComposeUiNode.Companion.getConstructor();
         int var22 = 6 | 896 & var15 << 6;
         int var23 = 0;
         ComposerKt.sourceInformationMarkerStart($composer, -553112988, "CC(ReusableComposeNode)N(factory,update,content)410@16187L9:Composables.kt#9igjgp");
         if (!($composer.getApplier() instanceof Applier)) {
            ComposablesKt.invalidApplier();
         }

         $composer.startReusableNode();
         if ($composer.getInserting()) {
            $composer.createNode(var20);
         } else {
            $composer.useNode();
         }

         Composer var24 = Updater.constructor-impl($composer);
         int var25 = 0;
         Updater.set-impl(var24, var11, ComposeUiNode.Companion.getSetMeasurePolicy());
         Updater.set-impl(var24, var18, ComposeUiNode.Companion.getSetResolvedCompositionLocals());
         Updater.set-impl(var24, var17, ComposeUiNode.Companion.getSetCompositeKeyHash());
         Updater.reconcile-impl(var24, ComposeUiNode.Companion.getApplyOnDeactivatedNodeAssertion());
         Updater.set-impl(var24, var19, ComposeUiNode.Companion.getSetModifier());
         int var26 = 14 & var22 >> 6;
         int var28 = 0;
         ComposerKt.sourceInformationMarkerStart($composer, 1833054614, "C72@3469L9:Box.kt#2w3rfo");
         int var29 = 6 | 112 & var9 >> 6;
         BoxScope var10000 = (BoxScope)BoxScopeInstance.INSTANCE;
         int var32 = 0;
         ComposerKt.sourceInformationMarkerStart($composer, -1368863963, "C464@22487L98:DetailComponents.kt#wxf82o");
         Modifier var36 = (Modifier)Modifier.Companion;
         int var33 = 22;
         int var34 = 0;
         Modifier var35 = SizeKt.size-3ABfNKs(var36, Dp.constructor-impl((float)var33));
         IconKt.Icon-ww6aTOc($icon, (String)null, var35, $color, $composer, 432, 0);
         ComposerKt.sourceInformationMarkerEnd($composer);
         ComposerKt.sourceInformationMarkerEnd($composer);
         $composer.endNode();
         ComposerKt.sourceInformationMarkerEnd($composer);
         ComposerKt.sourceInformationMarkerEnd($composer);
         ComposerKt.sourceInformationMarkerEnd($composer);
         if (ComposerKt.isTraceInProgress()) {
            ComposerKt.traceEventEnd();
         }
      } else {
         $composer.skipToGroupEnd();
      }

      return Unit.INSTANCE;
   }

   private static final Unit HeaderActionButton_iJQMabo$lambda$64(ImageVector $icon, long $color, Function0 $onClick, int $$changed, Composer $composer, int $force) {
      HeaderActionButton-iJQMabo($icon, $color, $onClick, $composer, RecomposeScopeImplKt.updateChangedFlags($$changed | 1));
      return Unit.INSTANCE;
   }

   private static final List<String> CurrentSnippetsModal$lambda$66(MutableState<List<String>> $localSnippets$delegate) {
      State var1 = (State)$localSnippets$delegate;
      Object var2 = null;
      KProperty var3 = null;
      int var4 = 0;
      return (List)var1.getValue();
   }

   private static final void CurrentSnippetsModal$lambda$67(MutableState<List<String>> $localSnippets$delegate, List<String> var1) {
      Object var3 = null;
      Object var4 = null;
      int var6 = 0;
      $localSnippets$delegate.setValue(var1);
   }

   private static final float CurrentSnippetsModal$lambda$94$lambda$70(MutableState<Float> $animateScale$delegate) {
      State var1 = (State)$animateScale$delegate;
      Object var2 = null;
      KProperty var3 = null;
      int var4 = 0;
      return ((Number)var1.getValue()).floatValue();
   }

   private static final void CurrentSnippetsModal$lambda$94$lambda$71(MutableState<Float> $animateScale$delegate, float var1) {
      Object var3 = null;
      Object var4 = null;
      Object var5 = var1;
      int var6 = 0;
      $animateScale$delegate.setValue(var5);
   }

   private static final float CurrentSnippetsModal$lambda$94$lambda$73(State<Float> $scaleFactor$delegate) {
      Object var2 = null;
      KProperty var3 = null;
      int var4 = 0;
      return ((Number)$scaleFactor$delegate.getValue()).floatValue();
   }

   private static final Unit CurrentSnippetsModal$lambda$94$lambda$75$lambda$74(State $scaleFactor$delegate, GraphicsLayerScope $this$graphicsLayer) {
      Intrinsics.checkNotNullParameter($this$graphicsLayer, "$this$graphicsLayer");
      $this$graphicsLayer.setScaleX(CurrentSnippetsModal$lambda$94$lambda$73($scaleFactor$delegate));
      $this$graphicsLayer.setScaleY(CurrentSnippetsModal$lambda$94$lambda$73($scaleFactor$delegate));
      return Unit.INSTANCE;
   }

   private static final Unit CurrentSnippetsModal$lambda$94$lambda$78$lambda$77() {
      return Unit.INSTANCE;
   }

   private static final Unit CurrentSnippetsModal$lambda$94$lambda$93$lambda$92$lambda$89$lambda$88$lambda$87$lambda$86$lambda$85$lambda$83$lambda$82(View $view, Function1 $onRemove, String $snippet, MutableState $localSnippets$delegate) {
      $view.performHapticFeedback(17);
      $onRemove.invoke($snippet);
      CurrentSnippetsModal$lambda$67($localSnippets$delegate, CollectionsKt.minus((Iterable)CurrentSnippetsModal$lambda$66($localSnippets$delegate), $snippet));
      return Unit.INSTANCE;
   }

   @Composable
   @ComposableTarget(
      applier = "androidx.compose.ui.UiComposable"
   )
   private static final Unit CurrentSnippetsModal$lambda$94$lambda$93$lambda$92$lambda$89$lambda$88$lambda$87$lambda$86$lambda$85$lambda$84(long $textColor, Composer $composer, int $changed) {
      ComposerKt.sourceInformation($composer, "C581@29053L82:DetailComponents.kt#wxf82o");
      if ($composer.shouldExecute(($changed & 3) != 2, $changed & 1)) {
         if (ComposerKt.isTraceInProgress()) {
            ComposerKt.traceEventStart(-24829075, $changed, -1, "com.android.snippets.ui.CurrentSnippetsModal.<anonymous>.<anonymous>.<anonymous>.<anonymous>.<anonymous>.<anonymous>.<anonymous>.<anonymous>.<anonymous> (DetailComponents.kt:581)");
         }

         ImageVector var4 = CloseKt.getClose(Icons.INSTANCE.getDefault());
         Modifier var10000 = (Modifier)Modifier.Companion;
         int var6 = 16;
         int var7 = 0;
         Modifier var5 = SizeKt.size-3ABfNKs(var10000, Dp.constructor-impl((float)var6));
         IconKt.Icon-ww6aTOc(var4, (String)null, var5, $textColor, $composer, 432, 0);
         if (ComposerKt.isTraceInProgress()) {
            ComposerKt.traceEventEnd();
         }
      } else {
         $composer.skipToGroupEnd();
      }

      return Unit.INSTANCE;
   }

   @Composable
   @ComposableTarget(
      applier = "androidx.compose.ui.UiComposable"
   )
   private static final Unit CurrentSnippetsModal$lambda$94$lambda$93$lambda$92$lambda$89$lambda$88$lambda$87$lambda$86(SnippetsViewModel $viewModel, String $snippet, long $textColor, View $view, Function1 $onRemove, MutableState $localSnippets$delegate, Composer $composer, int $changed) {
      ComposerKt.sourceInformation($composer, "C573@28193L1022:DetailComponents.kt#wxf82o");
      if ($composer.shouldExecute(($changed & 3) != 2, $changed & 1)) {
         if (ComposerKt.isTraceInProgress()) {
            ComposerKt.traceEventStart(460105195, $changed, -1, "com.android.snippets.ui.CurrentSnippetsModal.<anonymous>.<anonymous>.<anonymous>.<anonymous>.<anonymous>.<anonymous>.<anonymous> (DetailComponents.kt:573)");
         }

         Modifier.Companion var9 = Modifier.Companion;
         int var11 = 12;
         int var12 = 0;
         float var10 = Dp.constructor-impl((float)var11);
         var12 = 4;
         int var13 = 0;
         float var47 = Dp.constructor-impl((float)var12);
         var13 = 6;
         int var14 = 0;
         float var50 = Dp.constructor-impl((float)var13);
         var14 = 6;
         int var15 = 0;
         float var52 = Dp.constructor-impl((float)var14);
         Modifier var45 = PaddingKt.padding-qDBjuR0((Modifier)var9, var10, var50, var47, var52);
         Alignment.Vertical var48 = Alignment.Companion.getCenterVertically();
         int var53 = 384;
         var14 = 0;
         ComposerKt.sourceInformationMarkerStart($composer, 844473419, "CC(Row)N(modifier,horizontalArrangement,verticalAlignment,content)99@5125L58,100@5188L131:Row.kt#2w3rfo");
         Arrangement.Horizontal var46 = Arrangement.INSTANCE.getStart();
         MeasurePolicy var56 = RowKt.rowMeasurePolicy(var46, var48, $composer, 14 & var53 >> 3 | 112 & var53 >> 3);
         int var19 = 112 & var53 << 3;
         int var20 = 0;
         ComposerKt.sourceInformationMarkerStart($composer, -1159599143, "CC(Layout)N(content,modifier,measurePolicy)81@3355L27,84@3521L415:Layout.kt#80mrfh");
         int var21 = Long.hashCode(ComposablesKt.getCurrentCompositeKeyHashCode($composer, 0));
         CompositionLocalMap var22 = $composer.getCurrentCompositionLocalMap();
         Modifier var23 = ComposedModifierKt.materializeModifier($composer, var45);
         Function0 var24 = ComposeUiNode.Companion.getConstructor();
         int var26 = 6 | 896 & var19 << 6;
         int var27 = 0;
         ComposerKt.sourceInformationMarkerStart($composer, -553112988, "CC(ReusableComposeNode)N(factory,update,content)410@16187L9:Composables.kt#9igjgp");
         if (!($composer.getApplier() instanceof Applier)) {
            ComposablesKt.invalidApplier();
         }

         $composer.startReusableNode();
         if ($composer.getInserting()) {
            $composer.createNode(var24);
         } else {
            $composer.useNode();
         }

         Composer var28 = Updater.constructor-impl($composer);
         int var29 = 0;
         Updater.set-impl(var28, var56, ComposeUiNode.Companion.getSetMeasurePolicy());
         Updater.set-impl(var28, var22, ComposeUiNode.Companion.getSetResolvedCompositionLocals());
         Updater.set-impl(var28, var21, ComposeUiNode.Companion.getSetCompositeKeyHash());
         Updater.reconcile-impl(var28, ComposeUiNode.Companion.getApplyOnDeactivatedNodeAssertion());
         Updater.set-impl(var28, var23, ComposeUiNode.Companion.getSetModifier());
         int var30 = 14 & var26 >> 6;
         int var32 = 0;
         ComposerKt.sourceInformationMarkerStart($composer, 1456264949, "C101@5233L9:Row.kt#2w3rfo");
         int var33 = 6 | 112 & var53 >> 6;
         RowScope var10000 = (RowScope)RowScopeInstance.INSTANCE;
         int var36 = 0;
         ComposerKt.sourceInformationMarkerStart($composer, -97696563, "C574@28462L10,574@28392L92,574@28370L166,575@28577L39,576@28678L294,580@29007L170,576@28657L520:DetailComponents.kt#wxf82o");
         TextStyle var37 = getSnippetTextStyle($viewModel.getSnippetStyle($snippet), MaterialTheme.INSTANCE.getTypography($composer, MaterialTheme.$stable).getBodyMedium(), false, $composer, 0, 4);
         FontWeight var38 = FontWeight.Companion.getMedium();
         TextKt.Text-Nvy7gAk($snippet, (Modifier)null, $textColor, (TextAutoSize)null, 0L, (FontStyle)null, var38, (FontFamily)null, 0L, (TextDecoration)null, (TextAlign)null, 0L, 0, false, 0, 0, (Function1)null, var37, $composer, 1572864, 0, 131002);
         Modifier var62 = (Modifier)Modifier.Companion;
         int var57 = 4;
         int var60 = 0;
         SpacerKt.Spacer(SizeKt.width-3ABfNKs(var62, Dp.constructor-impl((float)var57)), $composer, 6);
         ComposerKt.sourceInformationMarkerStart($composer, 135405173, "CC(remember):DetailComponents.kt#9igjgp");
         boolean var39 = $composer.changedInstance($view) | $composer.changed($onRemove) | $composer.changed($snippet) | $composer.changed($localSnippets$delegate);
         int var40 = 0;
         Object var41 = $composer.rememberedValue();
         int var42 = 0;
         Object var63;
         if (!var39 && var41 != Composer.Companion.getEmpty()) {
            var63 = var41;
         } else {
            int var43 = 0;
            Object var44 = DetailComponentsKt::CurrentSnippetsModal$lambda$94$lambda$93$lambda$92$lambda$89$lambda$88$lambda$87$lambda$86$lambda$85$lambda$83$lambda$82;
            $composer.updateRememberedValue(var44);
            var63 = var44;
         }

         Function0 var58 = (Function0)var63;
         ComposerKt.sourceInformationMarkerEnd($composer);
         Function0 var64 = var58;
         Modifier var10001 = (Modifier)Modifier.Companion;
         int var59 = 24;
         var60 = 0;
         IconButtonKt.IconButton(var64, SizeKt.size-3ABfNKs(var10001, Dp.constructor-impl((float)var59)), false, (IconButtonColors)null, (MutableInteractionSource)null, (Shape)null, (Function2)ComposableLambdaKt.rememberComposableLambda(-24829075, true, DetailComponentsKt::CurrentSnippetsModal$lambda$94$lambda$93$lambda$92$lambda$89$lambda$88$lambda$87$lambda$86$lambda$85$lambda$84, $composer, 54), $composer, 1572912, 60);
         ComposerKt.sourceInformationMarkerEnd($composer);
         ComposerKt.sourceInformationMarkerEnd($composer);
         $composer.endNode();
         ComposerKt.sourceInformationMarkerEnd($composer);
         ComposerKt.sourceInformationMarkerEnd($composer);
         ComposerKt.sourceInformationMarkerEnd($composer);
         if (ComposerKt.isTraceInProgress()) {
            ComposerKt.traceEventEnd();
         }
      } else {
         $composer.skipToGroupEnd();
      }

      return Unit.INSTANCE;
   }

   @Composable
   @ComposableTarget(
      applier = "androidx.compose.ui.UiComposable"
   )
   private static final Unit CurrentSnippetsModal$lambda$94$lambda$93$lambda$92$lambda$89$lambda$88(MutableState $localSnippets$delegate, SnippetsViewModel $viewModel, View $view, Function1 $onRemove, FlowRowScope $this$FlowRow, Composer $composer, int $changed) {
      Intrinsics.checkNotNullParameter($this$FlowRow, "$this$FlowRow");
      ComposerKt.sourceInformation($composer, "C*555@26695L11,572@28155L1094,572@28067L1182:DetailComponents.kt#wxf82o");
      if ($composer.shouldExecute(($changed & 17) != 16, $changed & 1)) {
         if (ComposerKt.isTraceInProgress()) {
            ComposerKt.traceEventStart(-338898602, $changed, -1, "com.android.snippets.ui.CurrentSnippetsModal.<anonymous>.<anonymous>.<anonymous>.<anonymous>.<anonymous> (DetailComponents.kt:553)");
         }

         Iterable var7 = (Iterable)CurrentSnippetsModal$lambda$66($localSnippets$delegate);
         int var8 = 0;

         for(Object var10 : var7) {
            String var11 = (String)var10;
            int var12 = 0;
            Integer var13 = $viewModel.getSnippetColor(var11);
            long var14 = MaterialTheme.INSTANCE.getColorScheme($composer, MaterialTheme.$stable).getSurface-0d7_KjU();
            int var16 = 0;
            boolean var17 = !(Color.getRed-impl(var14) + Color.getGreen-impl(var14) + Color.getBlue-impl(var14) > 1.5F);
            long var10000;
            if (var13 != null) {
               $composer.startReplaceGroup(-814876612);
               $composer.endReplaceGroup();
               long var18 = ColorKt.Color(var13);
               float var20 = 0.299F * Color.getRed-impl(var18) + 0.587F * Color.getGreen-impl(var18) + 0.114F * Color.getBlue-impl(var18);
               var10000 = var17 && var20 < 0.3F ? Color.copy-wmQWz5c$default(var18, 0.0F, RangesKt.coerceAtMost(Color.getRed-impl(var18) + 0.4F, 1.0F), RangesKt.coerceAtMost(Color.getGreen-impl(var18) + 0.4F, 1.0F), RangesKt.coerceAtMost(Color.getBlue-impl(var18) + 0.4F, 1.0F), 1, (Object)null) : (!var17 && var20 > 0.7F ? Color.copy-wmQWz5c$default(var18, 0.0F, RangesKt.coerceAtLeast(Color.getRed-impl(var18) - 0.4F, 0.0F), RangesKt.coerceAtLeast(Color.getGreen-impl(var18) - 0.4F, 0.0F), RangesKt.coerceAtLeast(Color.getBlue-impl(var18) - 0.4F, 0.0F), 1, (Object)null) : var18);
            } else {
               $composer.startReplaceGroup(-814098605);
               ComposerKt.sourceInformation($composer, "565@27725L11");
               long var29 = MaterialTheme.INSTANCE.getColorScheme($composer, MaterialTheme.$stable).getPrimary-0d7_KjU();
               $composer.endReplaceGroup();
               var10000 = var29;
            }

            long var23_1 = var10000;
            long var30 = Color.copy-wmQWz5c$default(var23_1, 0.12F, 0.0F, 0.0F, 0.0F, 14, (Object)null);
            long var25 = Color.copy-wmQWz5c$default(var23_1, 0.3F, 0.0F, 0.0F, 0.0F, 14, (Object)null);
            Shape var10001 = (Shape)RoundedCornerShapeKt.getCircleShape();
            int var27 = 1;
            int var28 = 0;
            SurfaceKt.Surface-T9BRK9s((Modifier)null, var10001, var30, 0L, 0.0F, 0.0F, BorderStrokeKt.BorderStroke-cXLIe8U(Dp.constructor-impl((float)var27), var25), (Function2)ComposableLambdaKt.rememberComposableLambda(460105195, true, DetailComponentsKt::CurrentSnippetsModal$lambda$94$lambda$93$lambda$92$lambda$89$lambda$88$lambda$87$lambda$86, $composer, 54), $composer, 12582912, 57);
         }

         if (ComposerKt.isTraceInProgress()) {
            ComposerKt.traceEventEnd();
         }
      } else {
         $composer.skipToGroupEnd();
      }

      return Unit.INSTANCE;
   }

   private static final Unit CurrentSnippetsModal$lambda$94$lambda$93$lambda$92$lambda$91$lambda$90(View $view, Function0 $onClose) {
      $view.performHapticFeedback(16);
      $onClose.invoke();
      return Unit.INSTANCE;
   }

   @Composable
   @ComposableTarget(
      applier = "androidx.compose.ui.UiComposable"
   )
   private static final Unit CurrentSnippetsModal$lambda$94$lambda$93(View $view, Function0 $onClose, MutableState $localSnippets$delegate, SnippetsViewModel $viewModel, Function1 $onRemove, Composer $composer, int $changed) {
      ComposerKt.sourceInformation($composer, "C524@24488L5528:DetailComponents.kt#wxf82o");
      if ($composer.shouldExecute(($changed & 3) != 2, $changed & 1)) {
         if (ComposerKt.isTraceInProgress()) {
            ComposerKt.traceEventStart(-1801854879, $changed, -1, "com.android.snippets.ui.CurrentSnippetsModal.<anonymous>.<anonymous> (DetailComponents.kt:524)");
         }

         Modifier var7 = SizeKt.fillMaxWidth$default((Modifier)Modifier.Companion, 0.0F, 1, (Object)null);
         int var11 = 6;
         int var12 = 0;
         ComposerKt.sourceInformationMarkerStart($composer, 1341605231, "CC(Column)N(modifier,verticalArrangement,horizontalAlignment,content)87@4443L61,88@4509L134:Column.kt#2w3rfo");
         Arrangement.Vertical var8 = Arrangement.INSTANCE.getTop();
         Alignment.Horizontal var9 = Alignment.Companion.getStart();
         MeasurePolicy var13 = ColumnKt.columnMeasurePolicy(var8, var9, $composer, 14 & var11 >> 3 | 112 & var11 >> 3);
         int var17 = 112 & var11 << 3;
         int var18 = 0;
         ComposerKt.sourceInformationMarkerStart($composer, -1159599143, "CC(Layout)N(content,modifier,measurePolicy)81@3355L27,84@3521L415:Layout.kt#80mrfh");
         int var19 = Long.hashCode(ComposablesKt.getCurrentCompositeKeyHashCode($composer, 0));
         CompositionLocalMap var20 = $composer.getCurrentCompositionLocalMap();
         Modifier var21 = ComposedModifierKt.materializeModifier($composer, var7);
         Function0 var22 = ComposeUiNode.Companion.getConstructor();
         int var24 = 6 | 896 & var17 << 6;
         int var25 = 0;
         ComposerKt.sourceInformationMarkerStart($composer, -553112988, "CC(ReusableComposeNode)N(factory,update,content)410@16187L9:Composables.kt#9igjgp");
         if (!($composer.getApplier() instanceof Applier)) {
            ComposablesKt.invalidApplier();
         }

         $composer.startReusableNode();
         if ($composer.getInserting()) {
            $composer.createNode(var22);
         } else {
            $composer.useNode();
         }

         Composer var26 = Updater.constructor-impl($composer);
         int var27 = 0;
         Updater.set-impl(var26, var13, ComposeUiNode.Companion.getSetMeasurePolicy());
         Updater.set-impl(var26, var20, ComposeUiNode.Companion.getSetResolvedCompositionLocals());
         Updater.set-impl(var26, var19, ComposeUiNode.Companion.getSetCompositeKeyHash());
         Updater.reconcile-impl(var26, ComposeUiNode.Companion.getApplyOnDeactivatedNodeAssertion());
         Updater.set-impl(var26, var21, ComposeUiNode.Companion.getSetModifier());
         int var28 = 14 & var24 >> 6;
         int var30 = 0;
         ComposerKt.sourceInformationMarkerStart($composer, 2093002350, "C89@4557L9:Column.kt#2w3rfo");
         int var31 = 6 | 112 & var11 >> 6;
         ColumnScope var10000 = (ColumnScope)ColumnScopeInstance.INSTANCE;
         int var34 = 0;
         ComposerKt.sourceInformationMarkerStart($composer, -1163705479, "C526@24575L858,538@25490L11,538@25450L86,541@25597L3748,593@29497L11,594@29548L141,591@29405L597:DetailComponents.kt#wxf82o");
         Modifier var153 = SizeKt.fillMaxWidth$default((Modifier)Modifier.Companion, 0.0F, 1, (Object)null);
         int var35 = 24;
         int var36 = 0;
         float var10001 = Dp.constructor-impl((float)var35);
         var35 = 16;
         var36 = 0;
         Modifier var102 = PaddingKt.padding-VpY3zN4(var153, var10001, Dp.constructor-impl((float)var35));
         Arrangement.Horizontal var107 = (Arrangement.Horizontal)Arrangement.INSTANCE.getSpaceBetween();
         Alignment.Vertical var37 = Alignment.Companion.getCenterVertically();
         int var39 = 438;
         int var40 = 0;
         ComposerKt.sourceInformationMarkerStart($composer, 844473419, "CC(Row)N(modifier,horizontalArrangement,verticalAlignment,content)99@5125L58,100@5188L131:Row.kt#2w3rfo");
         MeasurePolicy var41 = RowKt.rowMeasurePolicy(var107, var37, $composer, 14 & var39 >> 3 | 112 & var39 >> 3);
         int var45 = 112 & var39 << 3;
         int var46 = 0;
         ComposerKt.sourceInformationMarkerStart($composer, -1159599143, "CC(Layout)N(content,modifier,measurePolicy)81@3355L27,84@3521L415:Layout.kt#80mrfh");
         int var47 = Long.hashCode(ComposablesKt.getCurrentCompositeKeyHashCode($composer, 0));
         CompositionLocalMap var48 = $composer.getCurrentCompositionLocalMap();
         Modifier var49 = ComposedModifierKt.materializeModifier($composer, var102);
         Function0 var50 = ComposeUiNode.Companion.getConstructor();
         int var52 = 6 | 896 & var45 << 6;
         int var53 = 0;
         ComposerKt.sourceInformationMarkerStart($composer, -553112988, "CC(ReusableComposeNode)N(factory,update,content)410@16187L9:Composables.kt#9igjgp");
         if (!($composer.getApplier() instanceof Applier)) {
            ComposablesKt.invalidApplier();
         }

         $composer.startReusableNode();
         if ($composer.getInserting()) {
            $composer.createNode(var50);
         } else {
            $composer.useNode();
         }

         Composer var54 = Updater.constructor-impl($composer);
         int var55 = 0;
         Updater.set-impl(var54, var41, ComposeUiNode.Companion.getSetMeasurePolicy());
         Updater.set-impl(var54, var48, ComposeUiNode.Companion.getSetResolvedCompositionLocals());
         Updater.set-impl(var54, var47, ComposeUiNode.Companion.getSetCompositeKeyHash());
         Updater.reconcile-impl(var54, ComposeUiNode.Companion.getApplyOnDeactivatedNodeAssertion());
         Updater.set-impl(var54, var49, ComposeUiNode.Companion.getSetModifier());
         int var56 = 14 & var52 >> 6;
         int var58 = 0;
         ComposerKt.sourceInformationMarkerStart($composer, 1456264949, "C101@5233L9:Row.kt#2w3rfo");
         int var59 = 6 | 112 & var39 >> 6;
         RowScope var154 = (RowScope)RowScopeInstance.INSTANCE;
         int var62 = 0;
         ComposerKt.sourceInformationMarkerStart($composer, -569889042, "C527@24825L10,527@24900L11,527@24781L141,534@25256L11,535@25361L11,528@24943L472:DetailComponents.kt#wxf82o");
         TextStyle var63 = MaterialTheme.INSTANCE.getTypography($composer, MaterialTheme.$stable).getTitleLarge();
         FontWeight var64 = FontWeight.Companion.getBold();
         long var65 = MaterialTheme.INSTANCE.getColorScheme($composer, MaterialTheme.$stable).getOnSurface-0d7_KjU();
         TextKt.Text-Nvy7gAk("Edit Snippets", (Modifier)null, var65, (TextAutoSize)null, 0L, (FontStyle)null, var64, (FontFamily)null, 0L, (TextDecoration)null, (TextAlign)null, 0L, 0, false, 0, 0, (Function1)null, var63, $composer, 1572870, 0, 131002);
         ImageVector var138 = CloseKt.getClose(Icons.INSTANCE.getDefault());
         int var67 = 40;
         int var68 = 0;
         float var144 = Dp.constructor-impl((float)var67);
         var65 = Color.copy-wmQWz5c$default(MaterialTheme.INSTANCE.getColorScheme($composer, MaterialTheme.$stable).getSecondaryContainer-0d7_KjU(), 0.5F, 0.0F, 0.0F, 0.0F, 14, (Object)null);
         long var69 = MaterialTheme.INSTANCE.getColorScheme($composer, MaterialTheme.$stable).getOnSecondaryContainer-0d7_KjU();
         AnimatedCookieButtonKt.AnimatedCookieButton-82CdiHg($onClose, var138, (Modifier)null, "Close", "Close", var65, var69, var144, (Shape)null, false, false, false, false, 0.0F, $composer, 12610560, 0, 16132);
         ComposerKt.sourceInformationMarkerEnd($composer);
         ComposerKt.sourceInformationMarkerEnd($composer);
         $composer.endNode();
         ComposerKt.sourceInformationMarkerEnd($composer);
         ComposerKt.sourceInformationMarkerEnd($composer);
         ComposerKt.sourceInformationMarkerEnd($composer);
         DividerKt.HorizontalDivider-9IZ8Weo((Modifier)null, 0.0F, Color.copy-wmQWz5c$default(MaterialTheme.INSTANCE.getColorScheme($composer, MaterialTheme.$stable).getOutlineVariant-0d7_KjU(), 0.3F, 0.0F, 0.0F, 0.0F, 14, (Object)null), $composer, 0, 3);
         Modifier var155 = SizeKt.fillMaxWidth$default((Modifier)Modifier.Companion, 0.0F, 1, (Object)null);
         int var103 = 24;
         int var108 = 0;
         Modifier var104 = PaddingKt.padding-3ABfNKs(var155, Dp.constructor-impl((float)var103));
         var39 = 6;
         var40 = 0;
         ComposerKt.sourceInformationMarkerStart($composer, 1341605231, "CC(Column)N(modifier,verticalArrangement,horizontalAlignment,content)87@4443L61,88@4509L134:Column.kt#2w3rfo");
         Arrangement.Vertical var109 = Arrangement.INSTANCE.getTop();
         Alignment.Horizontal var110 = Alignment.Companion.getStart();
         var41 = ColumnKt.columnMeasurePolicy(var109, var110, $composer, 14 & var39 >> 3 | 112 & var39 >> 3);
         var45 = 112 & var39 << 3;
         var46 = 0;
         ComposerKt.sourceInformationMarkerStart($composer, -1159599143, "CC(Layout)N(content,modifier,measurePolicy)81@3355L27,84@3521L415:Layout.kt#80mrfh");
         var47 = Long.hashCode(ComposablesKt.getCurrentCompositeKeyHashCode($composer, 0));
         var48 = $composer.getCurrentCompositionLocalMap();
         var49 = ComposedModifierKt.materializeModifier($composer, var104);
         var50 = ComposeUiNode.Companion.getConstructor();
         var52 = 6 | 896 & var45 << 6;
         var53 = 0;
         ComposerKt.sourceInformationMarkerStart($composer, -553112988, "CC(ReusableComposeNode)N(factory,update,content)410@16187L9:Composables.kt#9igjgp");
         if (!($composer.getApplier() instanceof Applier)) {
            ComposablesKt.invalidApplier();
         }

         $composer.startReusableNode();
         if ($composer.getInserting()) {
            $composer.createNode(var50);
         } else {
            $composer.useNode();
         }

         var54 = Updater.constructor-impl($composer);
         var55 = 0;
         Updater.set-impl(var54, var41, ComposeUiNode.Companion.getSetMeasurePolicy());
         Updater.set-impl(var54, var48, ComposeUiNode.Companion.getSetResolvedCompositionLocals());
         Updater.set-impl(var54, var47, ComposeUiNode.Companion.getSetCompositeKeyHash());
         Updater.reconcile-impl(var54, ComposeUiNode.Companion.getApplyOnDeactivatedNodeAssertion());
         Updater.set-impl(var54, var49, ComposeUiNode.Companion.getSetModifier());
         var56 = 14 & var52 >> 6;
         var58 = 0;
         ComposerKt.sourceInformationMarkerStart($composer, 2093002350, "C89@4557L9:Column.kt#2w3rfo");
         var59 = 6 | 112 & var39 >> 6;
         ColumnScope var156 = (ColumnScope)ColumnScopeInstance.INSTANCE;
         var62 = 0;
         ComposerKt.sourceInformationMarkerStart($composer, 1909618132, "C:DetailComponents.kt#wxf82o");
         if (CurrentSnippetsModal$lambda$66($localSnippets$delegate).isEmpty()) {
            $composer.startReplaceGroup(1909546335);
            ComposerKt.sourceInformation($composer, "543@25732L358");
            Modifier var157 = SizeKt.fillMaxWidth$default((Modifier)Modifier.Companion, 0.0F, 1, (Object)null);
            int var139 = 32;
            int var145 = 0;
            Modifier var140 = PaddingKt.padding-VpY3zN4$default(var157, 0.0F, Dp.constructor-impl((float)var139), 1, (Object)null);
            Alignment var146 = Alignment.Companion.getCenter();
            int var72 = 54;
            var67 = 0;
            ComposerKt.sourceInformationMarkerStart($composer, 1042775818, "CC(Box)N(modifier,contentAlignment,propagateMinConstraints,content)71@3424L131:Box.kt#2w3rfo");
            var68 = 0;
            MeasurePolicy var73 = BoxKt.maybeCachedBoxMeasurePolicy(var146, (boolean)var68);
            int var77 = 112 & var72 << 3;
            int var78 = 0;
            ComposerKt.sourceInformationMarkerStart($composer, -1159599143, "CC(Layout)N(content,modifier,measurePolicy)81@3355L27,84@3521L415:Layout.kt#80mrfh");
            int var79 = Long.hashCode(ComposablesKt.getCurrentCompositeKeyHashCode($composer, 0));
            CompositionLocalMap var80 = $composer.getCurrentCompositionLocalMap();
            Modifier var81 = ComposedModifierKt.materializeModifier($composer, var140);
            Function0 var82 = ComposeUiNode.Companion.getConstructor();
            int var84 = 6 | 896 & var77 << 6;
            int var85 = 0;
            ComposerKt.sourceInformationMarkerStart($composer, -553112988, "CC(ReusableComposeNode)N(factory,update,content)410@16187L9:Composables.kt#9igjgp");
            if (!($composer.getApplier() instanceof Applier)) {
               ComposablesKt.invalidApplier();
            }

            $composer.startReusableNode();
            if ($composer.getInserting()) {
               $composer.createNode(var82);
            } else {
               $composer.useNode();
            }

            Composer var86 = Updater.constructor-impl($composer);
            int var87 = 0;
            Updater.set-impl(var86, var73, ComposeUiNode.Companion.getSetMeasurePolicy());
            Updater.set-impl(var86, var80, ComposeUiNode.Companion.getSetResolvedCompositionLocals());
            Updater.set-impl(var86, var79, ComposeUiNode.Companion.getSetCompositeKeyHash());
            Updater.reconcile-impl(var86, ComposeUiNode.Companion.getApplyOnDeactivatedNodeAssertion());
            Updater.set-impl(var86, var81, ComposeUiNode.Companion.getSetModifier());
            int var88 = 14 & var84 >> 6;
            int var90 = 0;
            ComposerKt.sourceInformationMarkerStart($composer, 1833054614, "C72@3469L9:Box.kt#2w3rfo");
            int var91 = 6 | 112 & var72 >> 6;
            BoxScope var158 = (BoxScope)BoxScopeInstance.INSTANCE;
            int var94 = 0;
            ComposerKt.sourceInformationMarkerStart($composer, 855948142, "C544@25911L10,544@26016L11,544@25865L199:DetailComponents.kt#wxf82o");
            TextStyle var95 = MaterialTheme.INSTANCE.getTypography($composer, MaterialTheme.$stable).getBodyMedium();
            int var96 = FontStyle.Companion.getItalic-_-LCdwA();
            long var97 = Color.copy-wmQWz5c$default(MaterialTheme.INSTANCE.getColorScheme($composer, MaterialTheme.$stable).getOnSurfaceVariant-0d7_KjU(), 0.6F, 0.0F, 0.0F, 0.0F, 14, (Object)null);
            TextKt.Text-Nvy7gAk("No snippets yet", (Modifier)null, var97, (TextAutoSize)null, 0L, FontStyle.box-impl(var96), (FontWeight)null, (FontFamily)null, 0L, (TextDecoration)null, (TextAlign)null, 0L, 0, false, 0, 0, (Function1)null, var95, $composer, 6, 0, 131034);
            ComposerKt.sourceInformationMarkerEnd($composer);
            ComposerKt.sourceInformationMarkerEnd($composer);
            $composer.endNode();
            ComposerKt.sourceInformationMarkerEnd($composer);
            ComposerKt.sourceInformationMarkerEnd($composer);
            ComposerKt.sourceInformationMarkerEnd($composer);
            $composer.endReplaceGroup();
         } else {
            $composer.startReplaceGroup(1910041932);
            ComposerKt.sourceInformation($composer, "552@26485L2820,548@26205L3100");
            Modifier var159 = SizeKt.fillMaxWidth$default((Modifier)Modifier.Companion, 0.0F, 1, (Object)null);
            int var141 = 16;
            int var147 = 0;
            var159 = PaddingKt.padding-qDBjuR0$default(var159, 0.0F, 0.0F, 0.0F, Dp.constructor-impl((float)var141), 7, (Object)null);
            var141 = 8;
            var147 = 0;
            Arrangement.Horizontal var165 = (Arrangement.Horizontal)Arrangement.INSTANCE.spacedBy-0680j_4(Dp.constructor-impl((float)var141));
            var141 = 8;
            var147 = 0;
            FlowLayoutKt.FlowRow(var159, var165, (Arrangement.Vertical)Arrangement.INSTANCE.spacedBy-0680j_4(Dp.constructor-impl((float)var141)), (Alignment.Vertical)null, 0, 0, (Function3)ComposableLambdaKt.rememberComposableLambda(-338898602, true, DetailComponentsKt::CurrentSnippetsModal$lambda$94$lambda$93$lambda$92$lambda$89$lambda$88, $composer, 54), $composer, 1573302, 56);
            $composer.endReplaceGroup();
         }

         ComposerKt.sourceInformationMarkerEnd($composer);
         ComposerKt.sourceInformationMarkerEnd($composer);
         $composer.endNode();
         ComposerKt.sourceInformationMarkerEnd($composer);
         ComposerKt.sourceInformationMarkerEnd($composer);
         ComposerKt.sourceInformationMarkerEnd($composer);
         RoundedCornerShape var105 = RoundedCornerShapeKt.getCircleShape();
         long var99 = MaterialTheme.INSTANCE.getColorScheme($composer, MaterialTheme.$stable).getPrimary-0d7_KjU();
         Modifier var161 = SizeKt.fillMaxWidth$default((Modifier)Modifier.Companion, 0.0F, 1, (Object)null);
         var39 = 24;
         var40 = 0;
         var161 = PaddingKt.padding-VpY3zN4$default(var161, Dp.constructor-impl((float)var39), 0.0F, 2, (Object)null);
         var39 = 24;
         var40 = 0;
         var161 = PaddingKt.padding-qDBjuR0$default(var161, 0.0F, 0.0F, 0.0F, Dp.constructor-impl((float)var39), 7, (Object)null);
         var39 = 48;
         var40 = 0;
         Modifier var38 = SizeKt.height-3ABfNKs(var161, Dp.constructor-impl((float)var39));
         ComposerKt.sourceInformationMarkerStart($composer, 516804292, "CC(remember):DetailComponents.kt#9igjgp");
         boolean var121 = $composer.changedInstance($view) | $composer.changed($onClose);
         int var42 = 0;
         Object var43 = $composer.rememberedValue();
         int var57_1 = 0;
         Object var164;
         if (!var121 && var43 != Composer.Companion.getEmpty()) {
            var164 = var43;
         } else {
            var45 = 0;
            Object var125 = DetailComponentsKt::CurrentSnippetsModal$lambda$94$lambda$93$lambda$92$lambda$91$lambda$90;
            $composer.updateRememberedValue(var125);
            var164 = var125;
         }

         Function0 var115 = (Function0)var164;
         ComposerKt.sourceInformationMarkerEnd($composer);
         SurfaceKt.Surface-o_FOJdg(var115, var38, false, (Shape)var105, var99, 0L, 0.0F, 0.0F, (BorderStroke)null, (MutableInteractionSource)null, com.android.snippets.ui.ComposableSingletons.DetailComponentsKt.INSTANCE.getLambda$-677695070$app_debug(), $composer, 48, 6, 996);
         ComposerKt.sourceInformationMarkerEnd($composer);
         ComposerKt.sourceInformationMarkerEnd($composer);
         $composer.endNode();
         ComposerKt.sourceInformationMarkerEnd($composer);
         ComposerKt.sourceInformationMarkerEnd($composer);
         ComposerKt.sourceInformationMarkerEnd($composer);
         if (ComposerKt.isTraceInProgress()) {
            ComposerKt.traceEventEnd();
         }
      } else {
         $composer.skipToGroupEnd();
      }

      return Unit.INSTANCE;
   }

   private static final Unit CurrentSnippetsModal$lambda$95(Photo $photo, Function1 $onRemove, Function0 $onClose, SnippetsViewModel $viewModel, int $$changed, Composer $composer, int $force) {
      CurrentSnippetsModal($photo, $onRemove, $onClose, $viewModel, $composer, RecomposeScopeImplKt.updateChangedFlags($$changed | 1));
      return Unit.INSTANCE;
   }

   private static final String AddSnippetsModal$lambda$97(MutableState<String> $text$delegate) {
      State var1 = (State)$text$delegate;
      Object var2 = null;
      KProperty var3 = null;
      int var4 = 0;
      return (String)var1.getValue();
   }

   private static final void AddSnippetsModal$lambda$98(MutableState<String> $text$delegate, String var1) {
      Object var3 = null;
      Object var4 = null;
      int var6 = 0;
      $text$delegate.setValue(var1);
   }

   private static final Integer AddSnippetsModal$lambda$100(MutableState<Integer> $selectedColor$delegate) {
      State var1 = (State)$selectedColor$delegate;
      Object var2 = null;
      KProperty var3 = null;
      int var4 = 0;
      return (Integer)var1.getValue();
   }

   private static final void AddSnippetsModal$lambda$101(MutableState<Integer> $selectedColor$delegate, Integer var1) {
      Object var3 = null;
      Object var4 = null;
      int var6 = 0;
      $selectedColor$delegate.setValue(var1);
   }

   private static final SnippetStyle AddSnippetsModal$lambda$103(MutableState<SnippetStyle> $selectedStyle$delegate) {
      State var1 = (State)$selectedStyle$delegate;
      Object var2 = null;
      KProperty var3 = null;
      int var4 = 0;
      return (SnippetStyle)var1.getValue();
   }

   private static final void AddSnippetsModal$lambda$104(MutableState<SnippetStyle> $selectedStyle$delegate, SnippetStyle var1) {
      Object var3 = null;
      Object var4 = null;
      int var6 = 0;
      $selectedStyle$delegate.setValue(var1);
   }

   private static final int AddSnippetsModal$lambda$106(MutableIntState $selectedIndex$delegate) {
      IntState var1 = (IntState)$selectedIndex$delegate;
      Object var2 = null;
      KProperty var3 = null;
      int var4 = 0;
      return var1.getIntValue();
   }

   private static final void AddSnippetsModal$lambda$107(MutableIntState $selectedIndex$delegate, int var1) {
      Object var3 = null;
      Object var4 = null;
      int var6 = 0;
      $selectedIndex$delegate.setIntValue(var1);
   }

   private static final Unit AddSnippetsModal$lambda$158$lambda$157$lambda$152$lambda$112$lambda$111$lambda$110$lambda$109(View $view, int $index, MutableIntState $selectedIndex$delegate, boolean it) {
      $view.performHapticFeedback(4);
      AddSnippetsModal$lambda$107($selectedIndex$delegate, $index);
      return Unit.INSTANCE;
   }

   private static final Unit AddSnippetsModal$lambda$158$lambda$157$lambda$152$lambda$112$lambda$111(List $options, MutableIntState $selectedIndex$delegate, View $view, ButtonGroupScope $this$ButtonGroup) {
      Intrinsics.checkNotNullParameter($this$ButtonGroup, "$this$ButtonGroup");
      Iterable var4 = (Iterable)$options;
      int var5 = 0;
      int var6 = 0;

      for(Object var8 : var4) {
         int var11_1 = var6++;
         if (var11_1 < 0) {
            CollectionsKt.throwIndexOverflow();
         }

         String var10 = (String)var8;
         int var12 = 0;
         boolean var13 = var11_1 == AddSnippetsModal$lambda$106($selectedIndex$delegate);
         ButtonGroupScope.toggleableItem$default($this$ButtonGroup, var13, var10, DetailComponentsKt::AddSnippetsModal$lambda$158$lambda$157$lambda$152$lambda$112$lambda$111$lambda$110$lambda$109, (Function2)null, 1.0F, false, 40, (Object)null);
      }

      return Unit.INSTANCE;
   }

   private static final ContentTransform AddSnippetsModal$lambda$158$lambda$157$lambda$152$lambda$151$lambda$114$lambda$113(MotionScheme $motionScheme, AnimatedContentTransitionScope $this$AnimatedContent) {
      Intrinsics.checkNotNullParameter($this$AnimatedContent, "$this$AnimatedContent");
      return $this$AnimatedContent.using(AnimatedContentKt.togetherWith(EnterExitTransitionKt.fadeIn$default($motionScheme.defaultEffectsSpec(), 0.0F, 2, (Object)null), EnterExitTransitionKt.fadeOut$default($motionScheme.fastEffectsSpec(), 0.0F, 2, (Object)null)), AnimatedContentKt.SizeTransform$default(false, (Function2)null, 2, (Object)null));
   }

   private static final Unit AddSnippetsModal$lambda$158$lambda$157$lambda$152$lambda$151$lambda$150$lambda$149$lambda$116$lambda$115(View $view, KeyboardActionScope $this$KeyboardActions) {
      Intrinsics.checkNotNullParameter($this$KeyboardActions, "$this$KeyboardActions");
      $view.performHapticFeedback(4);
      return Unit.INSTANCE;
   }

   private static final Unit AddSnippetsModal$lambda$158$lambda$157$lambda$152$lambda$151$lambda$150$lambda$149$lambda$118$lambda$117(MutableState $text$delegate, String it) {
      Intrinsics.checkNotNullParameter(it, "it");
      if (it.length() <= 10) {
         AddSnippetsModal$lambda$98($text$delegate, it);
      }

      return Unit.INSTANCE;
   }

   @Composable
   @ComposableTarget(
      applier = "androidx.compose.ui.UiComposable"
   )
   private static final Unit AddSnippetsModal$lambda$158$lambda$157$lambda$152$lambda$151$lambda$150$lambda$149$lambda$119(MutableState $text$delegate, Composer $composer, int $changed) {
      ComposerKt.sourceInformation($composer, "C721@35717L10,717@35406L538:DetailComponents.kt#wxf82o");
      if ($composer.shouldExecute(($changed & 3) != 2, $changed & 1)) {
         if (ComposerKt.isTraceInProgress()) {
            ComposerKt.traceEventStart(-1855646235, $changed, -1, "com.android.snippets.ui.AddSnippetsModal.<anonymous>.<anonymous>.<anonymous>.<anonymous>.<anonymous>.<anonymous>.<anonymous> (DetailComponents.kt:717)");
         }

         String var3 = AddSnippetsModal$lambda$97($text$delegate).length() + "/10";
         Modifier var4 = SizeKt.fillMaxWidth$default((Modifier)Modifier.Companion, 0.0F, 1, (Object)null);
         int var5 = TextAlign.Companion.getEnd-e0LSkKk();
         TextStyle var6 = MaterialTheme.INSTANCE.getTypography($composer, MaterialTheme.$stable).getLabelSmall();
         long var10000;
         if (AddSnippetsModal$lambda$97($text$delegate).length() >= 10) {
            $composer.startReplaceGroup(-864588470);
            ComposerKt.sourceInformation($composer, "722@35833L11");
            long var9 = MaterialTheme.INSTANCE.getColorScheme($composer, MaterialTheme.$stable).getError-0d7_KjU();
            $composer.endReplaceGroup();
            var10000 = var9;
         } else {
            $composer.startReplaceGroup(-864587275);
            ComposerKt.sourceInformation($composer, "722@35870L11");
            long var11 = MaterialTheme.INSTANCE.getColorScheme($composer, MaterialTheme.$stable).getOnSurfaceVariant-0d7_KjU();
            $composer.endReplaceGroup();
            var10000 = var11;
         }

         long var7 = var10000;
         TextKt.Text-Nvy7gAk(var3, var4, var7, (TextAutoSize)null, 0L, (FontStyle)null, (FontWeight)null, (FontFamily)null, 0L, (TextDecoration)null, TextAlign.box-impl(var5), 0L, 0, false, 0, 0, (Function1)null, var6, $composer, 48, 0, 130040);
         if (ComposerKt.isTraceInProgress()) {
            ComposerKt.traceEventEnd();
         }
      } else {
         $composer.skipToGroupEnd();
      }

      return Unit.INSTANCE;
   }

   private static final Unit AddSnippetsModal$lambda$158$lambda$157$lambda$152$lambda$151$lambda$150$lambda$149$lambda$126$lambda$125$lambda$123$lambda$122(View $view, String $suggestion, MutableState $text$delegate) {
      $view.performHapticFeedback(4);
      AddSnippetsModal$lambda$98($text$delegate, $suggestion);
      return Unit.INSTANCE;
   }

   @Composable
   @ComposableTarget(
      applier = "androidx.compose.ui.UiComposable"
   )
   private static final Unit AddSnippetsModal$lambda$158$lambda$157$lambda$152$lambda$151$lambda$150$lambda$149$lambda$126$lambda$125$lambda$124(String $suggestion, Composer $composer, int $changed) {
      ComposerKt.sourceInformation($composer, "C758@38328L10,758@38289L62:DetailComponents.kt#wxf82o");
      if ($composer.shouldExecute(($changed & 3) != 2, $changed & 1)) {
         if (ComposerKt.isTraceInProgress()) {
            ComposerKt.traceEventStart(-695674214, $changed, -1, "com.android.snippets.ui.AddSnippetsModal.<anonymous>.<anonymous>.<anonymous>.<anonymous>.<anonymous>.<anonymous>.<anonymous>.<anonymous>.<anonymous> (DetailComponents.kt:758)");
         }

         TextKt.Text-Nvy7gAk($suggestion, (Modifier)null, 0L, (TextAutoSize)null, 0L, (FontStyle)null, (FontWeight)null, (FontFamily)null, 0L, (TextDecoration)null, (TextAlign)null, 0L, 0, false, 0, 0, (Function1)null, MaterialTheme.INSTANCE.getTypography($composer, MaterialTheme.$stable).getLabelMedium(), $composer, 0, 0, 131070);
         if (ComposerKt.isTraceInProgress()) {
            ComposerKt.traceEventEnd();
         }
      } else {
         $composer.skipToGroupEnd();
      }

      return Unit.INSTANCE;
   }

   @Composable
   @ComposableTarget(
      applier = "androidx.compose.ui.UiComposable"
   )
   private static final Unit AddSnippetsModal$lambda$158$lambda$157$lambda$152$lambda$151$lambda$150$lambda$149$lambda$126(List $suggestions, View $view, MutableState $text$delegate, FlowRowScope $this$FlowRow, Composer $composer, int $changed) {
      Intrinsics.checkNotNullParameter($this$FlowRow, "$this$FlowRow");
      ComposerKt.sourceInformation($composer, "C*754@37975L250,758@38287L66,761@38621L11,762@38738L11,760@38512L308,764@38916L11,753@37897L1115,766@39061L39:DetailComponents.kt#wxf82o");
      if ($composer.shouldExecute(($changed & 17) != 16, $changed & 1)) {
         if (ComposerKt.isTraceInProgress()) {
            ComposerKt.traceEventStart(-359467412, $changed, -1, "com.android.snippets.ui.AddSnippetsModal.<anonymous>.<anonymous>.<anonymous>.<anonymous>.<anonymous>.<anonymous>.<anonymous> (DetailComponents.kt:752)");
         }

         Iterable var6 = (Iterable)$suggestions;
         int var7 = 0;

         for(Object var9 : var6) {
            String var10 = (String)var9;
            int var11 = 0;
            ComposerKt.sourceInformationMarkerStart($composer, -1317427242, "CC(remember):DetailComponents.kt#9igjgp");
            boolean var13 = $composer.changedInstance($view) | $composer.changed(var10);
            int var14 = 0;
            Object var15 = $composer.rememberedValue();
            int var16 = 0;
            Object var10000;
            if (!var13 && var15 != Composer.Companion.getEmpty()) {
               var10000 = var15;
            } else {
               int var17 = 0;
               Object var18 = DetailComponentsKt::AddSnippetsModal$lambda$158$lambda$157$lambda$152$lambda$151$lambda$150$lambda$149$lambda$126$lambda$125$lambda$123$lambda$122;
               $composer.updateRememberedValue(var18);
               var10000 = var18;
            }

            Function0 var19 = (Function0)var10000;
            ComposerKt.sourceInformationMarkerEnd($composer);
            Function0 var23 = var19;
            Function2 var10001 = (Function2)ComposableLambdaKt.rememberComposableLambda(-695674214, true, DetailComponentsKt::AddSnippetsModal$lambda$158$lambda$157$lambda$152$lambda$151$lambda$150$lambda$149$lambda$126$lambda$125$lambda$124, $composer, 54);
            Shape var10005 = (Shape)RoundedCornerShapeKt.getCircleShape();
            ChipColors var10006 = SuggestionChipDefaults.INSTANCE.suggestionChipColors-5tl4gsc(MaterialTheme.INSTANCE.getColorScheme($composer, MaterialTheme.$stable).getSurfaceContainerHigh-0d7_KjU(), MaterialTheme.INSTANCE.getColorScheme($composer, MaterialTheme.$stable).getOnSurfaceVariant-0d7_KjU(), 0L, 0L, 0L, 0L, $composer, SuggestionChipDefaults.$stable << 18, 60);
            int var21 = 1;
            int var12 = 0;
            ChipKt.SuggestionChip(var23, var10001, (Modifier)null, false, (Function2)null, var10005, var10006, (ChipElevation)null, BorderStrokeKt.BorderStroke-cXLIe8U(Dp.constructor-impl((float)var21), Color.copy-wmQWz5c$default(MaterialTheme.INSTANCE.getColorScheme($composer, MaterialTheme.$stable).getOutlineVariant-0d7_KjU(), 0.5F, 0.0F, 0.0F, 0.0F, 14, (Object)null)), (MutableInteractionSource)null, $composer, 48, 668);
            Modifier var24 = (Modifier)Modifier.Companion;
            var21 = 8;
            var12 = 0;
            SpacerKt.Spacer(SizeKt.width-3ABfNKs(var24, Dp.constructor-impl((float)var21)), $composer, 6);
         }

         if (ComposerKt.isTraceInProgress()) {
            ComposerKt.traceEventEnd();
         }
      } else {
         $composer.skipToGroupEnd();
      }

      return Unit.INSTANCE;
   }

   private static final Unit AddSnippetsModal$lambda$158$lambda$157$lambda$152$lambda$151$lambda$150$lambda$149$lambda$139$lambda$138$lambda$133$lambda$129$lambda$128(View $view, boolean $isRandomSelected, MutableState $selectedColor$delegate) {
      $view.performHapticFeedback(4);
      AddSnippetsModal$lambda$101($selectedColor$delegate, $isRandomSelected ? null : -1);
      return Unit.INSTANCE;
   }

   @Composable
   @ComposableTarget(
      applier = "androidx.compose.ui.UiComposable"
   )
   private static final Unit AddSnippetsModal$lambda$158$lambda$157$lambda$152$lambda$151$lambda$150$lambda$149$lambda$139$lambda$138$lambda$133$lambda$132$lambda$131(boolean $isRandomSelected, Composer $composer, int $changed) {
      ComposerKt.sourceInformation($composer, "C800@41592L669:DetailComponents.kt#wxf82o");
      if ($composer.shouldExecute(($changed & 3) != 2, $changed & 1)) {
         if (ComposerKt.isTraceInProgress()) {
            ComposerKt.traceEventStart(1800646456, $changed, -1, "com.android.snippets.ui.AddSnippetsModal.<anonymous>.<anonymous>.<anonymous>.<anonymous>.<anonymous>.<anonymous>.<anonymous>.<anonymous>.<anonymous>.<anonymous>.<anonymous> (DetailComponents.kt:800)");
         }

         Alignment var4 = Alignment.Companion.getCenter();
         int var7 = 48;
         int var8 = 0;
         ComposerKt.sourceInformationMarkerStart($composer, 1042775818, "CC(Box)N(modifier,contentAlignment,propagateMinConstraints,content)71@3424L131:Box.kt#2w3rfo");
         Modifier var3 = (Modifier)Modifier.Companion;
         boolean var5 = false;
         MeasurePolicy var9 = BoxKt.maybeCachedBoxMeasurePolicy(var4, var5);
         int var13 = 112 & var7 << 3;
         int var14 = 0;
         ComposerKt.sourceInformationMarkerStart($composer, -1159599143, "CC(Layout)N(content,modifier,measurePolicy)81@3355L27,84@3521L415:Layout.kt#80mrfh");
         int var15 = Long.hashCode(ComposablesKt.getCurrentCompositeKeyHashCode($composer, 0));
         CompositionLocalMap var16 = $composer.getCurrentCompositionLocalMap();
         Modifier var17 = ComposedModifierKt.materializeModifier($composer, var3);
         Function0 var18 = ComposeUiNode.Companion.getConstructor();
         int var20 = 6 | 896 & var13 << 6;
         int var21 = 0;
         ComposerKt.sourceInformationMarkerStart($composer, -553112988, "CC(ReusableComposeNode)N(factory,update,content)410@16187L9:Composables.kt#9igjgp");
         if (!($composer.getApplier() instanceof Applier)) {
            ComposablesKt.invalidApplier();
         }

         $composer.startReusableNode();
         if ($composer.getInserting()) {
            $composer.createNode(var18);
         } else {
            $composer.useNode();
         }

         Composer var22 = Updater.constructor-impl($composer);
         int var23 = 0;
         Updater.set-impl(var22, var9, ComposeUiNode.Companion.getSetMeasurePolicy());
         Updater.set-impl(var22, var16, ComposeUiNode.Companion.getSetResolvedCompositionLocals());
         Updater.set-impl(var22, var15, ComposeUiNode.Companion.getSetCompositeKeyHash());
         Updater.reconcile-impl(var22, ComposeUiNode.Companion.getApplyOnDeactivatedNodeAssertion());
         Updater.set-impl(var22, var17, ComposeUiNode.Companion.getSetModifier());
         int var24 = 14 & var20 >> 6;
         int var26 = 0;
         ComposerKt.sourceInformationMarkerStart($composer, 1833054614, "C72@3469L9:Box.kt#2w3rfo");
         int var27 = 6 | 112 & var7 >> 6;
         BoxScope var10000 = (BoxScope)BoxScopeInstance.INSTANCE;
         int var30 = 0;
         ComposerKt.sourceInformationMarkerStart($composer, 708547341, "C801@41691L516:DetailComponents.kt#wxf82o");
         ImageVector var36 = ShuffleKt.getShuffle(Icons.INSTANCE.getDefault());
         Modifier var10002 = (Modifier)Modifier.Companion;
         int var31 = 28;
         int var32 = 0;
         var10002 = SizeKt.size-3ABfNKs(var10002, Dp.constructor-impl((float)var31));
         long var10003;
         if ($isRandomSelected) {
            $composer.startReplaceGroup(299963417);
            ComposerKt.sourceInformation($composer, "805@42082L11");
            long var33 = MaterialTheme.INSTANCE.getColorScheme($composer, MaterialTheme.$stable).getPrimary-0d7_KjU();
            $composer.endReplaceGroup();
            var10003 = var33;
         } else {
            $composer.startReplaceGroup(299964674);
            ComposerKt.sourceInformation($composer, "805@42121L11");
            long var35 = MaterialTheme.INSTANCE.getColorScheme($composer, MaterialTheme.$stable).getOnSurfaceVariant-0d7_KjU();
            $composer.endReplaceGroup();
            var10003 = var35;
         }

         IconKt.Icon-ww6aTOc(var36, "Shuffle", var10002, var10003, $composer, 432, 0);
         ComposerKt.sourceInformationMarkerEnd($composer);
         ComposerKt.sourceInformationMarkerEnd($composer);
         $composer.endNode();
         ComposerKt.sourceInformationMarkerEnd($composer);
         ComposerKt.sourceInformationMarkerEnd($composer);
         ComposerKt.sourceInformationMarkerEnd($composer);
         if (ComposerKt.isTraceInProgress()) {
            ComposerKt.traceEventEnd();
         }
      } else {
         $composer.skipToGroupEnd();
      }

      return Unit.INSTANCE;
   }

   @Composable
   @ComposableTarget(
      applier = "androidx.compose.ui.UiComposable"
   )
   private static final Unit AddSnippetsModal$lambda$158$lambda$157$lambda$152$lambda$151$lambda$150$lambda$149$lambda$139$lambda$138$lambda$133(View $view, MutableState $selectedColor$delegate, LazyGridItemScope $this$item, Composer $composer, int $changed) {
      Intrinsics.checkNotNullParameter($this$item, "$this$item");
      ComposerKt.sourceInformation($composer, "C787@40421L39,789@40590L281,782@40058L2299:DetailComponents.kt#wxf82o");
      if ($composer.shouldExecute(($changed & 17) != 16, $changed & 1)) {
         if (ComposerKt.isTraceInProgress()) {
            ComposerKt.traceEventStart(-1294572649, $changed, -1, "com.android.snippets.ui.AddSnippetsModal.<anonymous>.<anonymous>.<anonymous>.<anonymous>.<anonymous>.<anonymous>.<anonymous>.<anonymous>.<anonymous> (DetailComponents.kt:781)");
         }

         boolean var65;
         label58: {
            Integer var10000 = AddSnippetsModal$lambda$100($selectedColor$delegate);
            byte var6 = -1;
            if (var10000 != null) {
               if (var10000 == var6) {
                  var65 = true;
                  break label58;
               }
            }

            var65 = false;
         }

         boolean isRandomSelected = var65;
         Alignment var47 = Alignment.Companion.getCenter();
         Modifier var66 = (Modifier)Modifier.Companion;
         int var7 = 60;
         int var8 = 0;
         var66 = SizeKt.size-3ABfNKs(var66, Dp.constructor-impl((float)var7));
         ComposerKt.sourceInformationMarkerStart($composer, 1456216062, "CC(remember):DetailComponents.kt#9igjgp");
         boolean var9 = false;
         int var10 = 0;
         Object var11 = $composer.rememberedValue();
         int var12 = 0;
         Object var71;
         if (var11 == Composer.Companion.getEmpty()) {
            Modifier var40 = var66;
            int var13 = 0;
            MutableInteractionSource var10001 = InteractionSourceKt.MutableInteractionSource();
            var66 = var40;
            Object var58 = var10001;
            $composer.updateRememberedValue(var58);
            var71 = var58;
         } else {
            var71 = var11;
         }

         MutableInteractionSource var48 = (MutableInteractionSource)var71;
         ComposerKt.sourceInformationMarkerEnd($composer);
         MutableInteractionSource var72 = var48;
         Object var10002 = null;
         boolean var10003 = false;
         Object var10004 = null;
         Object var10005 = null;
         ComposerKt.sourceInformationMarkerStart($composer, 1456221712, "CC(remember):DetailComponents.kt#9igjgp");
         var9 = $composer.changedInstance($view) | $composer.changed(isRandomSelected);
         var10 = 0;
         var11 = $composer.rememberedValue();
         var12 = 0;
         Object var10006;
         if (!var9 && var11 != Composer.Companion.getEmpty()) {
            var10006 = var11;
         } else {
            Object var45 = null;
            Object var44 = null;
            boolean var43 = false;
            Object var42 = null;
            Modifier var64 = var66;
            int var59 = 0;
            Function0 var14_1 = DetailComponentsKt::AddSnippetsModal$lambda$158$lambda$157$lambda$152$lambda$151$lambda$150$lambda$149$lambda$139$lambda$138$lambda$133$lambda$129$lambda$128;
            var66 = var64;
            var72 = var48;
            var10002 = var42;
            var10003 = var43;
            var10004 = var44;
            var10005 = var45;
            $composer.updateRememberedValue(var14_1);
            var10006 = var14_1;
         }

         Function0 var49 = (Function0)var10006;
         ComposerKt.sourceInformationMarkerEnd($composer);
         Modifier var50 = ClickableKt.clickable-O2vRcR0$default(var66, var72, (Indication)var10002, var10003, (String)var10004, (Role)var10005, var49, 28, (Object)null);
         byte var55 = 48;
         var12 = 0;
         ComposerKt.sourceInformationMarkerStart($composer, 1042775818, "CC(Box)N(modifier,contentAlignment,propagateMinConstraints,content)71@3424L131:Box.kt#2w3rfo");
         var9 = false;
         MeasurePolicy var60 = BoxKt.maybeCachedBoxMeasurePolicy(var47, var9);
         int var17 = 112 & var55 << 3;
         int var18 = 0;
         ComposerKt.sourceInformationMarkerStart($composer, -1159599143, "CC(Layout)N(content,modifier,measurePolicy)81@3355L27,84@3521L415:Layout.kt#80mrfh");
         int var19 = Long.hashCode(ComposablesKt.getCurrentCompositeKeyHashCode($composer, 0));
         CompositionLocalMap var20 = $composer.getCurrentCompositionLocalMap();
         Modifier var21 = ComposedModifierKt.materializeModifier($composer, var50);
         Function0 var22 = ComposeUiNode.Companion.getConstructor();
         int var24 = 6 | 896 & var17 << 6;
         int var25 = 0;
         ComposerKt.sourceInformationMarkerStart($composer, -553112988, "CC(ReusableComposeNode)N(factory,update,content)410@16187L9:Composables.kt#9igjgp");
         if (!($composer.getApplier() instanceof Applier)) {
            ComposablesKt.invalidApplier();
         }

         $composer.startReusableNode();
         if ($composer.getInserting()) {
            $composer.createNode(var22);
         } else {
            $composer.useNode();
         }

         Composer var26 = Updater.constructor-impl($composer);
         int var27 = 0;
         Updater.set-impl(var26, var60, ComposeUiNode.Companion.getSetMeasurePolicy());
         Updater.set-impl(var26, var20, ComposeUiNode.Companion.getSetResolvedCompositionLocals());
         Updater.set-impl(var26, var19, ComposeUiNode.Companion.getSetCompositeKeyHash());
         Updater.reconcile-impl(var26, ComposeUiNode.Companion.getApplyOnDeactivatedNodeAssertion());
         Updater.set-impl(var26, var21, ComposeUiNode.Companion.getSetModifier());
         int var28 = 14 & var24 >> 6;
         int var30 = 0;
         ComposerKt.sourceInformationMarkerStart($composer, 1833054614, "C72@3469L9:Box.kt#2w3rfo");
         int var31 = 6 | 112 & var55 >> 6;
         BoxScope var68 = (BoxScope)BoxScopeInstance.INSTANCE;
         int var34 = 0;
         ComposerKt.sourceInformationMarkerStart($composer, -917088729, "C799@41538L773,794@40968L1343:DetailComponents.kt#wxf82o");
         Modifier var69 = (Modifier)Modifier.Companion;
         int var35 = 60;
         int var36 = 0;
         var69 = AspectRatioKt.aspectRatio$default(SizeKt.size-3ABfNKs(var69, Dp.constructor-impl((float)var35)), 1.0F, false, 2, (Object)null);
         Shape var73 = (Shape)RoundedCornerShapeKt.getCircleShape();
         long var74;
         if (isRandomSelected) {
            $composer.startReplaceGroup(-583764593);
            ComposerKt.sourceInformation($composer, "797@41247L11");
            long var37 = Color.copy-wmQWz5c$default(MaterialTheme.INSTANCE.getColorScheme($composer, MaterialTheme.$stable).getPrimary-0d7_KjU(), 0.1F, 0.0F, 0.0F, 0.0F, 14, (Object)null);
            $composer.endReplaceGroup();
            var74 = var37;
         } else {
            $composer.startReplaceGroup(-583762989);
            ComposerKt.sourceInformation($composer, "797@41305L11");
            long var63 = MaterialTheme.INSTANCE.getColorScheme($composer, MaterialTheme.$stable).getSurfaceContainerLowest-0d7_KjU();
            $composer.endReplaceGroup();
            var74 = var63;
         }

         BorderStroke var75;
         if (isRandomSelected) {
            $composer.startReplaceGroup(-583759534);
            ComposerKt.sourceInformation($composer, "798@41457L11");
            var36 = 3;
            int var39 = 0;
            BorderStroke var61 = BorderStrokeKt.BorderStroke-cXLIe8U(Dp.constructor-impl((float)var36), MaterialTheme.INSTANCE.getColorScheme($composer, MaterialTheme.$stable).getPrimary-0d7_KjU());
            $composer.endReplaceGroup();
            var75 = var61;
         } else {
            $composer.startReplaceGroup(-916619360);
            $composer.endReplaceGroup();
            var75 = null;
         }

         SurfaceKt.Surface-T9BRK9s(var69, var73, var74, 0L, 0.0F, 0.0F, var75, (Function2)ComposableLambdaKt.rememberComposableLambda(1800646456, true, DetailComponentsKt::AddSnippetsModal$lambda$158$lambda$157$lambda$152$lambda$151$lambda$150$lambda$149$lambda$139$lambda$138$lambda$133$lambda$132$lambda$131, $composer, 54), $composer, 12582918, 56);
         ComposerKt.sourceInformationMarkerEnd($composer);
         ComposerKt.sourceInformationMarkerEnd($composer);
         $composer.endNode();
         ComposerKt.sourceInformationMarkerEnd($composer);
         ComposerKt.sourceInformationMarkerEnd($composer);
         ComposerKt.sourceInformationMarkerEnd($composer);
         if (ComposerKt.isTraceInProgress()) {
            ComposerKt.traceEventEnd();
         }
      } else {
         $composer.skipToGroupEnd();
      }

      return Unit.INSTANCE;
   }

   private static final Unit AddSnippetsModal$lambda$158$lambda$157$lambda$152$lambda$151$lambda$150$lambda$149$lambda$139$lambda$138(List $snippetColorsPalette, View $view, MutableState $selectedColor$delegate, LazyGridScope $this$LazyVerticalGrid) {
      Intrinsics.checkNotNullParameter($this$LazyVerticalGrid, "$this$LazyVerticalGrid");
      LazyGridScope.item$default($this$LazyVerticalGrid, (Object)null, (Function1)null, (Object)null, (Function3)ComposableLambdaKt.composableLambdaInstance(-1294572649, true, DetailComponentsKt::AddSnippetsModal$lambda$158$lambda$157$lambda$152$lambda$151$lambda$150$lambda$149$lambda$139$lambda$138$lambda$133), 7, (Object)null);
      Function1 var6 = null;
      Function2 var7 = null;
      Function1 var8 = (Function1)1.INSTANCE;
      int var9 = 0;
      $this$LazyVerticalGrid.items($snippetColorsPalette.size(), (Function1)null, (Function2)null, (Function1)(new AddSnippetsModal.lambda.158.lambda.157.lambda.152.lambda.151.lambda.150.lambda.149.lambda.139.lambda.138..inlined.items.default.4(var8, $snippetColorsPalette)), (Function4)ComposableLambdaKt.composableLambdaInstance(-1117249557, true, new AddSnippetsModal.lambda.158.lambda.157.lambda.152.lambda.151.lambda.150.lambda.149.lambda.139.lambda.138..inlined.items.default.5($snippetColorsPalette, $view, $selectedColor$delegate)));
      return Unit.INSTANCE;
   }

   private static final Unit AddSnippetsModal$lambda$158$lambda$157$lambda$152$lambda$151$lambda$150$lambda$149$lambda$148$lambda$147$lambda$146$lambda$142$lambda$141(View $view, SnippetStyle $style, MutableState $selectedStyle$delegate) {
      $view.performHapticFeedback(4);
      AddSnippetsModal$lambda$104($selectedStyle$delegate, $style);
      return Unit.INSTANCE;
   }

   @Composable
   @ComposableTarget(
      applier = "androidx.compose.ui.UiComposable"
   )
   private static final Unit AddSnippetsModal$lambda$158$lambda$157$lambda$152$lambda$151$lambda$150$lambda$149$lambda$148$lambda$147$lambda$146$lambda$145$lambda$144(SnippetStyle $style, boolean $isSelected, Composer $composer, int $changed) {
      ComposerKt.sourceInformation($composer, "C868@46826L606:DetailComponents.kt#wxf82o");
      if ($composer.shouldExecute(($changed & 3) != 2, $changed & 1)) {
         if (ComposerKt.isTraceInProgress()) {
            ComposerKt.traceEventStart(246129283, $changed, -1, "com.android.snippets.ui.AddSnippetsModal.<anonymous>.<anonymous>.<anonymous>.<anonymous>.<anonymous>.<anonymous>.<anonymous>.<anonymous>.<anonymous>.<anonymous>.<anonymous> (DetailComponents.kt:868)");
         }

         Alignment var5 = Alignment.Companion.getCenter();
         int var8 = 48;
         int var9 = 0;
         ComposerKt.sourceInformationMarkerStart($composer, 1042775818, "CC(Box)N(modifier,contentAlignment,propagateMinConstraints,content)71@3424L131:Box.kt#2w3rfo");
         Modifier var4 = (Modifier)Modifier.Companion;
         boolean var6 = false;
         MeasurePolicy var10 = BoxKt.maybeCachedBoxMeasurePolicy(var5, var6);
         int var14 = 112 & var8 << 3;
         int var15 = 0;
         ComposerKt.sourceInformationMarkerStart($composer, -1159599143, "CC(Layout)N(content,modifier,measurePolicy)81@3355L27,84@3521L415:Layout.kt#80mrfh");
         int var16 = Long.hashCode(ComposablesKt.getCurrentCompositeKeyHashCode($composer, 0));
         CompositionLocalMap var17 = $composer.getCurrentCompositionLocalMap();
         Modifier var18 = ComposedModifierKt.materializeModifier($composer, var4);
         Function0 var19 = ComposeUiNode.Companion.getConstructor();
         int var21 = 6 | 896 & var14 << 6;
         int var22 = 0;
         ComposerKt.sourceInformationMarkerStart($composer, -553112988, "CC(ReusableComposeNode)N(factory,update,content)410@16187L9:Composables.kt#9igjgp");
         if (!($composer.getApplier() instanceof Applier)) {
            ComposablesKt.invalidApplier();
         }

         $composer.startReusableNode();
         if ($composer.getInserting()) {
            $composer.createNode(var19);
         } else {
            $composer.useNode();
         }

         Composer var23 = Updater.constructor-impl($composer);
         int var24 = 0;
         Updater.set-impl(var23, var10, ComposeUiNode.Companion.getSetMeasurePolicy());
         Updater.set-impl(var23, var17, ComposeUiNode.Companion.getSetResolvedCompositionLocals());
         Updater.set-impl(var23, var16, ComposeUiNode.Companion.getSetCompositeKeyHash());
         Updater.reconcile-impl(var23, ComposeUiNode.Companion.getApplyOnDeactivatedNodeAssertion());
         Updater.set-impl(var23, var18, ComposeUiNode.Companion.getSetModifier());
         int var25 = 14 & var21 >> 6;
         int var27 = 0;
         ComposerKt.sourceInformationMarkerStart($composer, 1833054614, "C72@3469L9:Box.kt#2w3rfo");
         int var28 = 6 | 112 & var8 >> 6;
         BoxScope var10000 = (BoxScope)BoxScopeInstance.INSTANCE;
         int var31 = 0;
         ComposerKt.sourceInformationMarkerStart($composer, 409479753, "C871@47124L10,871@47083L63,869@46929L445:DetailComponents.kt#wxf82o");
         TextStyle var32 = getSnippetTextStyle($style, MaterialTheme.INSTANCE.getTypography($composer, MaterialTheme.$stable).getTitleLarge(), false, $composer, 0, 4);
         long var38;
         if ($isSelected) {
            $composer.startReplaceGroup(1952881894);
            ComposerKt.sourceInformation($composer, "872@47250L11");
            long var33 = MaterialTheme.INSTANCE.getColorScheme($composer, MaterialTheme.$stable).getOnPrimary-0d7_KjU();
            $composer.endReplaceGroup();
            var38 = var33;
         } else {
            $composer.startReplaceGroup(1952883206);
            ComposerKt.sourceInformation($composer, "872@47291L11");
            long var37 = MaterialTheme.INSTANCE.getColorScheme($composer, MaterialTheme.$stable).getOnSurface-0d7_KjU();
            $composer.endReplaceGroup();
            var38 = var37;
         }

         long var35 = var38;
         TextKt.Text-Nvy7gAk("S", (Modifier)null, var35, (TextAutoSize)null, 0L, (FontStyle)null, (FontWeight)null, (FontFamily)null, 0L, (TextDecoration)null, (TextAlign)null, 0L, 0, false, 0, 0, (Function1)null, var32, $composer, 6, 0, 131066);
         ComposerKt.sourceInformationMarkerEnd($composer);
         ComposerKt.sourceInformationMarkerEnd($composer);
         $composer.endNode();
         ComposerKt.sourceInformationMarkerEnd($composer);
         ComposerKt.sourceInformationMarkerEnd($composer);
         ComposerKt.sourceInformationMarkerEnd($composer);
         if (ComposerKt.isTraceInProgress()) {
            ComposerKt.traceEventEnd();
         }
      } else {
         $composer.skipToGroupEnd();
      }

      return Unit.INSTANCE;
   }

   @Composable
   @ComposableTarget(
      applier = "androidx.compose.ui.UiComposable"
   )
   private static final Unit AddSnippetsModal$lambda$158$lambda$157$lambda$152$lambda$151$lambda$150$lambda$149$lambda$148(MutableState $selectedStyle$delegate, View $view, FlowRowScope $this$FlowRow, Composer $composer, int $changed) {
      Intrinsics.checkNotNullParameter($this$FlowRow, "$this$FlowRow");
      ComposerKt.sourceInformation($composer, "C*846@44990L3060:DetailComponents.kt#wxf82o");
      if ($composer.shouldExecute(($changed & 17) != 16, $changed & 1)) {
         if (ComposerKt.isTraceInProgress()) {
            ComposerKt.traceEventStart(1437886559, $changed, -1, "com.android.snippets.ui.AddSnippetsModal.<anonymous>.<anonymous>.<anonymous>.<anonymous>.<anonymous>.<anonymous>.<anonymous> (DetailComponents.kt:844)");
         }

         Object[] var5 = SnippetStyle.values();
         int var6 = 0;
         int var7 = 0;

         for(int var8 = var5.length; var7 < var8; ++var7) {
            Object var9 = var5[var7];
            int var11 = 0;
            boolean var12 = AddSnippetsModal$lambda$103($selectedStyle$delegate) == var9;
            Alignment.Horizontal var13 = Alignment.Companion.getCenterHorizontally();
            Modifier var10000 = (Modifier)Modifier.Companion;
            int var14 = 8;
            int var15 = 0;
            Modifier var86 = PaddingKt.padding-VpY3zN4$default(var10000, Dp.constructor-impl((float)var14), 0.0F, 2, (Object)null);
            short var18 = 390;
            int var19 = 0;
            ComposerKt.sourceInformationMarkerStart($composer, 1341605231, "CC(Column)N(modifier,verticalArrangement,horizontalAlignment,content)87@4443L61,88@4509L134:Column.kt#2w3rfo");
            Arrangement.Vertical var87 = Arrangement.INSTANCE.getTop();
            MeasurePolicy var20 = ColumnKt.columnMeasurePolicy(var87, var13, $composer, 14 & var18 >> 3 | 112 & var18 >> 3);
            int var24 = 112 & var18 << 3;
            int var25 = 0;
            ComposerKt.sourceInformationMarkerStart($composer, -1159599143, "CC(Layout)N(content,modifier,measurePolicy)81@3355L27,84@3521L415:Layout.kt#80mrfh");
            int var26 = Long.hashCode(ComposablesKt.getCurrentCompositeKeyHashCode($composer, 0));
            CompositionLocalMap var27 = $composer.getCurrentCompositionLocalMap();
            Modifier var28 = ComposedModifierKt.materializeModifier($composer, var86);
            Function0 var29 = ComposeUiNode.Companion.getConstructor();
            int var31 = 6 | 896 & var24 << 6;
            int var32 = 0;
            ComposerKt.sourceInformationMarkerStart($composer, -553112988, "CC(ReusableComposeNode)N(factory,update,content)410@16187L9:Composables.kt#9igjgp");
            if (!($composer.getApplier() instanceof Applier)) {
               ComposablesKt.invalidApplier();
            }

            $composer.startReusableNode();
            if ($composer.getInserting()) {
               $composer.createNode(var29);
            } else {
               $composer.useNode();
            }

            Composer var33 = Updater.constructor-impl($composer);
            int var34 = 0;
            Updater.set-impl(var33, var20, ComposeUiNode.Companion.getSetMeasurePolicy());
            Updater.set-impl(var33, var27, ComposeUiNode.Companion.getSetResolvedCompositionLocals());
            Updater.set-impl(var33, var26, ComposeUiNode.Companion.getSetCompositeKeyHash());
            Updater.reconcile-impl(var33, ComposeUiNode.Companion.getApplyOnDeactivatedNodeAssertion());
            Updater.set-impl(var33, var28, ComposeUiNode.Companion.getSetModifier());
            int var35 = 14 & var31 >> 6;
            int var37 = 0;
            ComposerKt.sourceInformationMarkerStart($composer, 2093002350, "C89@4557L9:Column.kt#2w3rfo");
            int var38 = 6 | 112 & var18 >> 6;
            ColumnScope var109 = (ColumnScope)ColumnScopeInstance.INSTANCE;
            int var41 = 0;
            ComposerKt.sourceInformationMarkerStart($composer, -1308805210, "C855@45672L39,857@45849L264,850@45289L2247,879@47736L10,880@47833L11,877@47585L419:DetailComponents.kt#wxf82o");
            Alignment var42 = Alignment.Companion.getCenter();
            Modifier var110 = (Modifier)Modifier.Companion;
            int var43 = 60;
            int var44 = 0;
            var110 = SizeKt.size-3ABfNKs(var110, Dp.constructor-impl((float)var43));
            ComposerKt.sourceInformationMarkerStart($composer, -42209943, "CC(remember):DetailComponents.kt#9igjgp");
            boolean var45 = false;
            int var46 = 0;
            Object var47 = $composer.rememberedValue();
            int var48 = 0;
            Object var116;
            if (var47 == Composer.Companion.getEmpty()) {
               Modifier var49 = var110;
               int var50 = 0;
               MutableInteractionSource var10001 = InteractionSourceKt.MutableInteractionSource();
               var110 = var49;
               Object var103 = var10001;
               $composer.updateRememberedValue(var103);
               var116 = var103;
            } else {
               var116 = var47;
            }

            MutableInteractionSource var89 = (MutableInteractionSource)var116;
            ComposerKt.sourceInformationMarkerEnd($composer);
            MutableInteractionSource var117 = var89;
            Object var10002 = null;
            boolean var10003 = false;
            Object var10004 = null;
            Object var10005 = null;
            ComposerKt.sourceInformationMarkerStart($composer, -42204054, "CC(remember):DetailComponents.kt#9igjgp");
            var45 = $composer.changedInstance($view) | $composer.changed(((Enum)var9).ordinal());
            var46 = 0;
            var47 = $composer.rememberedValue();
            var48 = 0;
            Object var10006;
            if (!var45 && var47 != Composer.Companion.getEmpty()) {
               var10006 = var47;
            } else {
               Object var51 = null;
               Object var52 = null;
               boolean var53 = false;
               Object var54 = null;
               Modifier var102 = var110;
               int var104 = 0;
               Function0 var57_1 = DetailComponentsKt::AddSnippetsModal$lambda$158$lambda$157$lambda$152$lambda$151$lambda$150$lambda$149$lambda$148$lambda$147$lambda$146$lambda$142$lambda$141;
               var110 = var102;
               var117 = var89;
               var10002 = var54;
               var10003 = var53;
               var10004 = var52;
               var10005 = var51;
               $composer.updateRememberedValue(var57_1);
               var10006 = var57_1;
            }

            Function0 var90 = (Function0)var10006;
            ComposerKt.sourceInformationMarkerEnd($composer);
            Modifier var91 = ClickableKt.clickable-O2vRcR0$default(var110, var117, (Indication)var10002, var10003, (String)var10004, (Role)var10005, var90, 28, (Object)null);
            int var98 = 48;
            var48 = 0;
            ComposerKt.sourceInformationMarkerStart($composer, 1042775818, "CC(Box)N(modifier,contentAlignment,propagateMinConstraints,content)71@3424L131:Box.kt#2w3rfo");
            var45 = false;
            MeasurePolicy var105 = BoxKt.maybeCachedBoxMeasurePolicy(var42, var45);
            int var60 = 112 & var98 << 3;
            int var61 = 0;
            ComposerKt.sourceInformationMarkerStart($composer, -1159599143, "CC(Layout)N(content,modifier,measurePolicy)81@3355L27,84@3521L415:Layout.kt#80mrfh");
            int var62 = Long.hashCode(ComposablesKt.getCurrentCompositeKeyHashCode($composer, 0));
            CompositionLocalMap var63 = $composer.getCurrentCompositionLocalMap();
            Modifier var64 = ComposedModifierKt.materializeModifier($composer, var91);
            Function0 var65 = ComposeUiNode.Companion.getConstructor();
            int var67 = 6 | 896 & var60 << 6;
            int var68 = 0;
            ComposerKt.sourceInformationMarkerStart($composer, -553112988, "CC(ReusableComposeNode)N(factory,update,content)410@16187L9:Composables.kt#9igjgp");
            if (!($composer.getApplier() instanceof Applier)) {
               ComposablesKt.invalidApplier();
            }

            $composer.startReusableNode();
            if ($composer.getInserting()) {
               $composer.createNode(var65);
            } else {
               $composer.useNode();
            }

            Composer var69 = Updater.constructor-impl($composer);
            int var70 = 0;
            Updater.set-impl(var69, var105, ComposeUiNode.Companion.getSetMeasurePolicy());
            Updater.set-impl(var69, var63, ComposeUiNode.Companion.getSetResolvedCompositionLocals());
            Updater.set-impl(var69, var62, ComposeUiNode.Companion.getSetCompositeKeyHash());
            Updater.reconcile-impl(var69, ComposeUiNode.Companion.getApplyOnDeactivatedNodeAssertion());
            Updater.set-impl(var69, var64, ComposeUiNode.Companion.getSetModifier());
            int var71 = 14 & var67 >> 6;
            int var73 = 0;
            ComposerKt.sourceInformationMarkerStart($composer, 1833054614, "C72@3469L9:Box.kt#2w3rfo");
            int var74 = 6 | 112 & var98 >> 6;
            BoxScope var112 = (BoxScope)BoxScopeInstance.INSTANCE;
            int var77 = 0;
            ComposerKt.sourceInformationMarkerStart($composer, 1789512999, "C867@46768L718,862@46218L1268:DetailComponents.kt#wxf82o");
            Modifier var113 = (Modifier)Modifier.Companion;
            int var78 = 60;
            int var79 = 0;
            var113 = SizeKt.size-3ABfNKs(var113, Dp.constructor-impl((float)var78));
            Shape var118 = (Shape)RoundedCornerShapeKt.getCircleShape();
            long var119;
            if (var12) {
               $composer.startReplaceGroup(-1743381361);
               ComposerKt.sourceInformation($composer, "865@46487L11");
               long var80 = MaterialTheme.INSTANCE.getColorScheme($composer, MaterialTheme.$stable).getPrimary-0d7_KjU();
               $composer.endReplaceGroup();
               var119 = var80;
            } else {
               $composer.startReplaceGroup(-1743380098);
               ComposerKt.sourceInformation($composer, "865@46526L11");
               long var108 = MaterialTheme.INSTANCE.getColorScheme($composer, MaterialTheme.$stable).getSurfaceContainerLowest-0d7_KjU();
               $composer.endReplaceGroup();
               var119 = var108;
            }

            BorderStroke var120;
            if (var12) {
               $composer.startReplaceGroup(1789895413);
               $composer.endReplaceGroup();
               var120 = null;
            } else {
               $composer.startReplaceGroup(-1743376380);
               ComposerKt.sourceInformation($composer, "866@46686L11");
               var79 = 2;
               int var82 = 0;
               BorderStroke var106 = BorderStrokeKt.BorderStroke-cXLIe8U(Dp.constructor-impl((float)var79), MaterialTheme.INSTANCE.getColorScheme($composer, MaterialTheme.$stable).getOutlineVariant-0d7_KjU());
               $composer.endReplaceGroup();
               var120 = var106;
            }

            SurfaceKt.Surface-T9BRK9s(var113, var118, var119, 0L, 0.0F, 0.0F, var120, (Function2)ComposableLambdaKt.rememberComposableLambda(246129283, true, DetailComponentsKt::AddSnippetsModal$lambda$158$lambda$157$lambda$152$lambda$151$lambda$150$lambda$149$lambda$148$lambda$147$lambda$146$lambda$145$lambda$144, $composer, 54), $composer, 12582918, 56);
            ComposerKt.sourceInformationMarkerEnd($composer);
            ComposerKt.sourceInformationMarkerEnd($composer);
            $composer.endNode();
            ComposerKt.sourceInformationMarkerEnd($composer);
            ComposerKt.sourceInformationMarkerEnd($composer);
            ComposerKt.sourceInformationMarkerEnd($composer);
            String var88 = ((SnippetStyle)var9).name();
            TextStyle var83 = MaterialTheme.INSTANCE.getTypography($composer, MaterialTheme.$stable).getLabelSmall();
            long var84 = MaterialTheme.INSTANCE.getColorScheme($composer, MaterialTheme.$stable).getOnSurfaceVariant-0d7_KjU();
            var113 = (Modifier)Modifier.Companion;
            var46 = 4;
            var98 = 0;
            Modifier var94 = PaddingKt.padding-qDBjuR0$default(var113, 0.0F, Dp.constructor-impl((float)var46), 0.0F, 0.0F, 13, (Object)null);
            TextKt.Text-Nvy7gAk(var88, var94, var84, (TextAutoSize)null, 0L, (FontStyle)null, (FontWeight)null, (FontFamily)null, 0L, (TextDecoration)null, (TextAlign)null, 0L, 0, false, 0, 0, (Function1)null, var83, $composer, 48, 0, 131064);
            ComposerKt.sourceInformationMarkerEnd($composer);
            ComposerKt.sourceInformationMarkerEnd($composer);
            $composer.endNode();
            ComposerKt.sourceInformationMarkerEnd($composer);
            ComposerKt.sourceInformationMarkerEnd($composer);
            ComposerKt.sourceInformationMarkerEnd($composer);
         }

         if (ComposerKt.isTraceInProgress()) {
            ComposerKt.traceEventEnd();
         }
      } else {
         $composer.skipToGroupEnd();
      }

      return Unit.INSTANCE;
   }

   @Composable
   @ComposableTarget(
      applier = "androidx.compose.ui.UiComposable"
   )
   private static final Unit AddSnippetsModal$lambda$158$lambda$157$lambda$152$lambda$151$lambda$150(View $view, SnippetsViewModel $viewModel, List $snippetColorsPalette, MutableState $text$delegate, MutableState $selectedColor$delegate, MutableState $selectedStyle$delegate, AnimatedContentScope $this$AnimatedContent, int step, Composer $composer, int $changed) {
      Intrinsics.checkNotNullParameter($this$AnimatedContent, "$this$AnimatedContent");
      ComposerKt.sourceInformation($composer, "CN(step)699@33918L14302:DetailComponents.kt#wxf82o");
      if (ComposerKt.isTraceInProgress()) {
         ComposerKt.traceEventStart(-1041146982, $changed, -1, "com.android.snippets.ui.AddSnippetsModal.<anonymous>.<anonymous>.<anonymous>.<anonymous>.<anonymous> (DetailComponents.kt:699)");
      }

      Alignment.Horizontal var12 = Alignment.Companion.getCenterHorizontally();
      int var14 = 384;
      int var15 = 0;
      ComposerKt.sourceInformationMarkerStart($composer, 1341605231, "CC(Column)N(modifier,verticalArrangement,horizontalAlignment,content)87@4443L61,88@4509L134:Column.kt#2w3rfo");
      Modifier var10 = (Modifier)Modifier.Companion;
      Arrangement.Vertical var11 = Arrangement.INSTANCE.getTop();
      MeasurePolicy var16 = ColumnKt.columnMeasurePolicy(var11, var12, $composer, 14 & var14 >> 3 | 112 & var14 >> 3);
      int var20 = 112 & var14 << 3;
      int var21 = 0;
      ComposerKt.sourceInformationMarkerStart($composer, -1159599143, "CC(Layout)N(content,modifier,measurePolicy)81@3355L27,84@3521L415:Layout.kt#80mrfh");
      int var22 = Long.hashCode(ComposablesKt.getCurrentCompositeKeyHashCode($composer, 0));
      CompositionLocalMap var23 = $composer.getCurrentCompositionLocalMap();
      Modifier var24 = ComposedModifierKt.materializeModifier($composer, var10);
      Function0 var25 = ComposeUiNode.Companion.getConstructor();
      int var27 = 6 | 896 & var20 << 6;
      int var28 = 0;
      ComposerKt.sourceInformationMarkerStart($composer, -553112988, "CC(ReusableComposeNode)N(factory,update,content)410@16187L9:Composables.kt#9igjgp");
      if (!($composer.getApplier() instanceof Applier)) {
         ComposablesKt.invalidApplier();
      }

      $composer.startReusableNode();
      if ($composer.getInserting()) {
         $composer.createNode(var25);
      } else {
         $composer.useNode();
      }

      Composer var29 = Updater.constructor-impl($composer);
      int var30 = 0;
      Updater.set-impl(var29, var16, ComposeUiNode.Companion.getSetMeasurePolicy());
      Updater.set-impl(var29, var23, ComposeUiNode.Companion.getSetResolvedCompositionLocals());
      Updater.set-impl(var29, var22, ComposeUiNode.Companion.getSetCompositeKeyHash());
      Updater.reconcile-impl(var29, ComposeUiNode.Companion.getApplyOnDeactivatedNodeAssertion());
      Updater.set-impl(var29, var24, ComposeUiNode.Companion.getSetModifier());
      int var31 = 14 & var27 >> 6;
      int var33 = 0;
      ComposerKt.sourceInformationMarkerStart($composer, 2093002350, "C89@4557L9:Column.kt#2w3rfo");
      int var34 = 6 | 112 & var14 >> 6;
      ColumnScope var10000 = (ColumnScope)ColumnScopeInstance.INSTANCE;
      int var37 = 0;
      ComposerKt.sourceInformationMarkerStart($composer, -54164396, "C:DetailComponents.kt#wxf82o");
      switch (step) {
         case 0:
            $composer.startReplaceGroup(-54392340);
            ComposerKt.sourceInformation($composer, "706@34411L10,712@35038L11,713@35140L11,714@35240L11,711@34951L350,727@36219L158,704@34233L34,716@35360L626,702@34104L2353,734@36569L576");
            String var84 = AddSnippetsModal$lambda$97($text$delegate);
            Modifier var86 = SizeKt.fillMaxWidth$default((Modifier)Modifier.Companion, 0.0F, 1, (Object)null);
            TextStyle var90 = TextStyle.copy-p1EtxEg$default(MaterialTheme.INSTANCE.getTypography($composer, MaterialTheme.$stable).getBodyLarge(), 0L, 0L, FontWeight.Companion.getSemiBold(), (FontStyle)null, (FontSynthesis)null, (FontFamily)null, (String)null, 0L, (BaselineShift)null, (TextGeometricTransform)null, (LocaleList)null, 0L, (TextDecoration)null, (Shadow)null, (DrawStyle)null, 0, 0, 0L, (TextIndent)null, (PlatformTextStyle)null, (LineHeightStyle)null, 0, 0, (TextMotion)null, 16777211, (Object)null);
            int var96 = 16;
            int var100 = 0;
            RoundedCornerShape var38_7 = RoundedCornerShapeKt.RoundedCornerShape-0680j_4(Dp.constructor-impl((float)var96));
            OutlinedTextFieldDefaults var101 = OutlinedTextFieldDefaults.INSTANCE;
            long var44 = MaterialTheme.INSTANCE.getColorScheme($composer, MaterialTheme.$stable).getPrimary-0d7_KjU();
            long var46 = MaterialTheme.INSTANCE.getColorScheme($composer, MaterialTheme.$stable).getOutlineVariant-0d7_KjU();
            long var48 = MaterialTheme.INSTANCE.getColorScheme($composer, MaterialTheme.$stable).getPrimary-0d7_KjU();
            TextFieldColors var97 = var101.colors-0hiis_0(0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, var48, 0L, (TextSelectionColors)null, var44, var46, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, $composer, 0, 0, 0, 0, 3072, 2147477247, 4095);
            KeyboardOptions var102 = new KeyboardOptions(0, (Boolean)null, 0, ImeAction.Companion.getDone-eUduSuo(), (PlatformImeOptions)null, (Boolean)null, (LocaleList)null, 119, (DefaultConstructorMarker)null);
            ComposerKt.sourceInformationMarkerStart($composer, 1799424834, "CC(remember):DetailComponents.kt#9igjgp");
            boolean var108 = $composer.changedInstance($view);
            int var111 = 0;
            Object var114 = $composer.rememberedValue();
            int var54 = 0;
            Object var131;
            if (!var108 && var114 != Composer.Companion.getEmpty()) {
               var131 = var114;
            } else {
               int var57 = 0;
               Object var59 = DetailComponentsKt::AddSnippetsModal$lambda$158$lambda$157$lambda$152$lambda$151$lambda$150$lambda$149$lambda$116$lambda$115;
               $composer.updateRememberedValue(var59);
               var131 = var59;
            }

            Function1 var122 = (Function1)var131;
            ComposerKt.sourceInformationMarkerEnd($composer);
            Object var75 = null;
            byte var76 = 62;
            Object var77 = null;
            Object var78 = null;
            Object var79 = null;
            Object var80 = null;
            Object var81 = null;
            KeyboardActions var125 = new KeyboardActions(var122, (Function1)var81, (Function1)var80, (Function1)var79, (Function1)var78, (Function1)var77, var76, (DefaultConstructorMarker)var75);
            String var132 = var84;
            ComposerKt.sourceInformationMarkerStart($composer, 1799361158, "CC(remember):DetailComponents.kt#9igjgp");
            var108 = (boolean)0;
            var111 = 0;
            var114 = $composer.rememberedValue();
            var54 = 0;
            Object var141;
            if (var114 == Composer.Companion.getEmpty()) {
               int var118 = 0;
               Function1 var140 = DetailComponentsKt::AddSnippetsModal$lambda$158$lambda$157$lambda$152$lambda$151$lambda$150$lambda$149$lambda$118$lambda$117;
               var132 = var84;
               Object var120 = var140;
               $composer.updateRememberedValue(var120);
               var141 = var120;
            } else {
               var141 = var114;
            }

            var122 = (Function1)var141;
            ComposerKt.sourceInformationMarkerEnd($composer);
            OutlinedTextFieldKt.OutlinedTextField(var132, var122, var86, false, false, var90, com.android.snippets.ui.ComposableSingletons.DetailComponentsKt.INSTANCE.getLambda$1097060242$app_debug(), com.android.snippets.ui.ComposableSingletons.DetailComponentsKt.INSTANCE.getLambda$-1715883629$app_debug(), (Function2)null, (Function2)null, (Function2)null, (Function2)null, (Function2)ComposableLambdaKt.rememberComposableLambda(-1855646235, true, DetailComponentsKt::AddSnippetsModal$lambda$158$lambda$157$lambda$152$lambda$151$lambda$150$lambda$149$lambda$119, $composer, 54), false, (VisualTransformation)null, var102, var125, true, 0, 0, (MutableInteractionSource)null, (Shape)var38_7, var97, $composer, 14156208, 12779904, 0, 1863448);
            String var87 = AddSnippetsModal$lambda$97($text$delegate);
            List var91 = $viewModel.getAllUniqueSnippets();
            ComposerKt.sourceInformationMarkerStart($composer, 1799436452, "CC(remember):DetailComponents.kt#9igjgp");
            boolean var103 = $composer.changed(var87) | $composer.changed(var91);
            int var126 = 0;
            Object var124 = $composer.rememberedValue();
            int var107 = 0;
            Object var134;
            if (!var103 && var124 != Composer.Companion.getEmpty()) {
               var134 = var124;
            } else {
               var108 = (boolean)0;
               List var133;
               if (((CharSequence)AddSnippetsModal$lambda$97($text$delegate)).length() == 0) {
                  var133 = CollectionsKt.take((Iterable)$viewModel.getAllUniqueSnippets(), 5);
               } else {
                  Iterable var113 = (Iterable)$viewModel.getAllUniqueSnippets();
                  int var116 = 0;
                  Collection var119 = (Collection)(new ArrayList());
                  int var121 = 0;

                  for(Object var63 : var113) {
                     String var64 = (String)var63;
                     int var65 = 0;
                     if (StringsKt.contains((CharSequence)var64, (CharSequence)AddSnippetsModal$lambda$97($text$delegate), true) && !StringsKt.equals(var64, AddSnippetsModal$lambda$97($text$delegate), true)) {
                        var119.add(var63);
                     }
                  }

                  var133 = CollectionsKt.take((Iterable)((List)var119), 8);
               }

               Object var66 = var133;
               $composer.updateRememberedValue(var66);
               var134 = var66;
            }

            List var38_7 = (List)var134;
            ComposerKt.sourceInformationMarkerEnd($composer);
            if (!((Collection)var38_7).isEmpty()) {
               $composer.startReplaceGroup(-51326843);
               ComposerKt.sourceInformation($composer, "751@37767L1421,746@37368L1820");
               Modifier var135 = SizeKt.fillMaxWidth$default((Modifier)Modifier.Companion, 0.0F, 1, (Object)null);
               int var88 = 8;
               int var92 = 0;
               var135 = PaddingKt.padding-qDBjuR0$default(var135, 0.0F, Dp.constructor-impl((float)var88), 0.0F, 0.0F, 13, (Object)null);
               Arrangement.Horizontal var142 = (Arrangement.Horizontal)Arrangement.INSTANCE.getCenter();
               var88 = 8;
               var92 = 0;
               FlowLayoutKt.FlowRow(var135, var142, (Arrangement.Vertical)Arrangement.INSTANCE.spacedBy-0680j_4(Dp.constructor-impl((float)var88)), (Alignment.Vertical)null, 4, 0, (Function3)ComposableLambdaKt.rememberComposableLambda(-359467412, true, DetailComponentsKt::AddSnippetsModal$lambda$158$lambda$157$lambda$152$lambda$151$lambda$150$lambda$149$lambda$126, $composer, 54), $composer, 1597878, 40);
               $composer.endReplaceGroup();
            } else {
               $composer.startReplaceGroup(-88340130);
               $composer.endReplaceGroup();
            }

            $composer.endReplaceGroup();
            Unit var137 = Unit.INSTANCE;
            break;
         case 1:
            $composer.startReplaceGroup(-49202785);
            ComposerKt.sourceInformation($composer, "779@39878L4313,772@39350L4841");
            GridCells.Fixed var83 = new GridCells.Fixed(4);
            Modifier var85 = SizeKt.fillMaxSize$default((Modifier)Modifier.Companion, 0.0F, 1, (Object)null);
            int var38_7 = 16;
            int var41 = 0;
            Arrangement.HorizontalOrVertical var40 = Arrangement.INSTANCE.spacedBy-0680j_4(Dp.constructor-impl((float)var38_7));
            var41 = 16;
            int var42 = 0;
            Arrangement.HorizontalOrVertical var38_7 = Arrangement.INSTANCE.spacedBy-0680j_4(Dp.constructor-impl((float)var41));
            var42 = 8;
            int var61 = 0;
            PaddingValues var95 = PaddingKt.PaddingValues-0680j_4(Dp.constructor-impl((float)var42));
            GridCells var129 = (GridCells)var83;
            Modifier var139 = var85;
            Object var10002 = null;
            PaddingValues var10003 = var95;
            boolean var10004 = false;
            Arrangement.Vertical var10005 = (Arrangement.Vertical)var38_7;
            Arrangement.Horizontal var10006 = (Arrangement.Horizontal)var40;
            Object var10007 = null;
            boolean var10008 = true;
            Object var10009 = null;
            ComposerKt.sourceInformationMarkerStart($composer, 1799546077, "CC(remember):DetailComponents.kt#9igjgp");
            boolean var60 = $composer.changedInstance($view) | $composer.changedInstance($snippetColorsPalette);
            int var50 = 0;
            Object var51 = $composer.rememberedValue();
            int var52 = 0;
            Object var10010;
            if (!var60 && var51 != Composer.Companion.getEmpty()) {
               var10010 = var51;
            } else {
               Object var67 = null;
               boolean var68 = true;
               Object var69 = null;
               Arrangement.Horizontal var70 = var10006;
               Arrangement.Vertical var71 = var10005;
               boolean var72 = false;
               Object var58 = null;
               GridCells var56 = var129;
               int var53 = 0;
               Function1 var54_1 = DetailComponentsKt::AddSnippetsModal$lambda$158$lambda$157$lambda$152$lambda$151$lambda$150$lambda$149$lambda$139$lambda$138;
               var129 = var56;
               var139 = var85;
               var10002 = var58;
               var10003 = var95;
               var10004 = var72;
               var10005 = var71;
               var10006 = var70;
               var10007 = var69;
               var10008 = var68;
               var10009 = var67;
               $composer.updateRememberedValue(var54_1);
               var10010 = var54_1;
            }

            Function1 var99 = (Function1)var10010;
            ComposerKt.sourceInformationMarkerEnd($composer);
            LazyGridDslKt.LazyVerticalGrid(var129, var139, (LazyGridState)var10002, var10003, var10004, var10005, var10006, (FlingBehavior)var10007, var10008, (OverscrollEffect)var10009, var99, $composer, 102435888, 0, 660);
            $composer.endReplaceGroup();
            Unit var130 = Unit.INSTANCE;
            break;
         case 2:
            $composer.startReplaceGroup(-44309311);
            ComposerKt.sourceInformation($composer, "843@44748L3382,838@44388L3742");
            Modifier var127 = SizeKt.fillMaxWidth$default((Modifier)Modifier.Companion, 0.0F, 1, (Object)null);
            Arrangement.Horizontal var10001 = (Arrangement.Horizontal)Arrangement.INSTANCE.getCenter();
            int var38 = 16;
            int var39 = 0;
            FlowLayoutKt.FlowRow(var127, var10001, (Arrangement.Vertical)Arrangement.INSTANCE.spacedBy-0680j_4(Dp.constructor-impl((float)var38)), (Alignment.Vertical)null, 3, 0, (Function3)ComposableLambdaKt.rememberComposableLambda(1437886559, true, DetailComponentsKt::AddSnippetsModal$lambda$158$lambda$157$lambda$152$lambda$151$lambda$150$lambda$149$lambda$148, $composer, 54), $composer, 1597878, 40);
            $composer.endReplaceGroup();
            Unit var128 = Unit.INSTANCE;
            break;
         default:
            $composer.startReplaceGroup(-88340130);
            $composer.endReplaceGroup();
            Unit var138 = Unit.INSTANCE;
      }

      ComposerKt.sourceInformationMarkerEnd($composer);
      ComposerKt.sourceInformationMarkerEnd($composer);
      $composer.endNode();
      ComposerKt.sourceInformationMarkerEnd($composer);
      ComposerKt.sourceInformationMarkerEnd($composer);
      ComposerKt.sourceInformationMarkerEnd($composer);
      if (ComposerKt.isTraceInProgress()) {
         ComposerKt.traceEventEnd();
      }

      return Unit.INSTANCE;
   }

   private static final Unit AddSnippetsModal$lambda$158$lambda$157$lambda$154$lambda$153(View $view, List $snippetColorsPalette, Function3 $onAdd, Function0 $onClose, MutableState $selectedColor$delegate, MutableState $text$delegate, MutableState $selectedStyle$delegate) {
      int var10;
      label22: {
         $view.performHapticFeedback(16);
         if (AddSnippetsModal$lambda$100($selectedColor$delegate) != null) {
            label21: {
               Integer var10000 = AddSnippetsModal$lambda$100($selectedColor$delegate);
               byte var8 = -1;
               if (var10000 != null) {
                  if (var10000 == var8) {
                     break label21;
                  }
               }

               var10000 = AddSnippetsModal$lambda$100($selectedColor$delegate);
               Intrinsics.checkNotNull(var10000);
               var10 = var10000;
               break label22;
            }
         }

         var10 = ((Number)CollectionsKt.random((Collection)$snippetColorsPalette, (Random)Random.Default)).intValue();
      }

      int finalColor = var10;
      $onAdd.invoke(StringsKt.trim((CharSequence)AddSnippetsModal$lambda$97($text$delegate)).toString(), finalColor, AddSnippetsModal$lambda$103($selectedStyle$delegate));
      $onClose.invoke();
      return Unit.INSTANCE;
   }

   @Composable
   @ComposableTarget(
      applier = "androidx.compose.ui.UiComposable"
   )
   private static final Unit AddSnippetsModal$lambda$158$lambda$157$lambda$156(int $localSnippetsCount, Composer $composer, int $changed) {
      ComposerKt.sourceInformation($composer, "C907@48951L483:DetailComponents.kt#wxf82o");
      if ($composer.shouldExecute(($changed & 3) != 2, $changed & 1)) {
         if (ComposerKt.isTraceInProgress()) {
            ComposerKt.traceEventStart(841083423, $changed, -1, "com.android.snippets.ui.AddSnippetsModal.<anonymous>.<anonymous>.<anonymous> (DetailComponents.kt:907)");
         }

         Alignment.Vertical var3 = Alignment.Companion.getCenterVertically();
         Arrangement.Horizontal var5 = (Arrangement.Horizontal)Arrangement.INSTANCE.getCenter();
         short var8 = 432;
         int var9 = 0;
         ComposerKt.sourceInformationMarkerStart($composer, 844473419, "CC(Row)N(modifier,horizontalArrangement,verticalAlignment,content)99@5125L58,100@5188L131:Row.kt#2w3rfo");
         Modifier var4 = (Modifier)Modifier.Companion;
         MeasurePolicy var10 = RowKt.rowMeasurePolicy(var5, var3, $composer, 14 & var8 >> 3 | 112 & var8 >> 3);
         int var14 = 112 & var8 << 3;
         int var15 = 0;
         ComposerKt.sourceInformationMarkerStart($composer, -1159599143, "CC(Layout)N(content,modifier,measurePolicy)81@3355L27,84@3521L415:Layout.kt#80mrfh");
         int var16 = Long.hashCode(ComposablesKt.getCurrentCompositeKeyHashCode($composer, 0));
         CompositionLocalMap var17 = $composer.getCurrentCompositionLocalMap();
         Modifier var18 = ComposedModifierKt.materializeModifier($composer, var4);
         Function0 var19 = ComposeUiNode.Companion.getConstructor();
         int var21 = 6 | 896 & var14 << 6;
         int var22 = 0;
         ComposerKt.sourceInformationMarkerStart($composer, -553112988, "CC(ReusableComposeNode)N(factory,update,content)410@16187L9:Composables.kt#9igjgp");
         if (!($composer.getApplier() instanceof Applier)) {
            ComposablesKt.invalidApplier();
         }

         $composer.startReusableNode();
         if ($composer.getInserting()) {
            $composer.createNode(var19);
         } else {
            $composer.useNode();
         }

         Composer var23 = Updater.constructor-impl($composer);
         int var24 = 0;
         Updater.set-impl(var23, var10, ComposeUiNode.Companion.getSetMeasurePolicy());
         Updater.set-impl(var23, var17, ComposeUiNode.Companion.getSetResolvedCompositionLocals());
         Updater.set-impl(var23, var16, ComposeUiNode.Companion.getSetCompositeKeyHash());
         Updater.reconcile-impl(var23, ComposeUiNode.Companion.getApplyOnDeactivatedNodeAssertion());
         Updater.set-impl(var23, var18, ComposeUiNode.Companion.getSetModifier());
         int var25 = 14 & var21 >> 6;
         int var27 = 0;
         ComposerKt.sourceInformationMarkerStart($composer, 1456264949, "C101@5233L9:Row.kt#2w3rfo");
         int var28 = 6 | 112 & var8 >> 6;
         RowScope var10000 = (RowScope)RowScopeInstance.INSTANCE;
         int var31 = 0;
         ComposerKt.sourceInformationMarkerStart($composer, 1845938655, "C913@49249L11,915@49372L10,911@49127L289:DetailComponents.kt#wxf82o");
         TextKt.Text-Nvy7gAk("Add Snippet (" + $localSnippetsCount + "/6)", (Modifier)null, MaterialTheme.INSTANCE.getColorScheme($composer, MaterialTheme.$stable).getOnPrimary-0d7_KjU(), (TextAutoSize)null, 0L, (FontStyle)null, FontWeight.Companion.getBold(), (FontFamily)null, 0L, (TextDecoration)null, (TextAlign)null, 0L, 0, false, 0, 0, (Function1)null, MaterialTheme.INSTANCE.getTypography($composer, MaterialTheme.$stable).getTitleMedium(), $composer, 1572864, 0, 131002);
         ComposerKt.sourceInformationMarkerEnd($composer);
         ComposerKt.sourceInformationMarkerEnd($composer);
         $composer.endNode();
         ComposerKt.sourceInformationMarkerEnd($composer);
         ComposerKt.sourceInformationMarkerEnd($composer);
         ComposerKt.sourceInformationMarkerEnd($composer);
         if (ComposerKt.isTraceInProgress()) {
            ComposerKt.traceEventEnd();
         }
      } else {
         $composer.skipToGroupEnd();
      }

      return Unit.INSTANCE;
   }

   @Composable
   @ComposableTarget(
      applier = "androidx.compose.ui.UiComposable"
   )
   private static final Unit AddSnippetsModal$lambda$158(View $view, List $snippetColorsPalette, Function3 $onAdd, Function0 $onClose, List $options, MutableIntState $selectedIndex$delegate, SnippetsViewModel $viewModel, MutableState $text$delegate, MutableState $selectedColor$delegate, MutableState $selectedStyle$delegate, int $localSnippetsCount, ColumnScope $this$ModalBottomSheet, Composer $composer, int $changed) {
      Intrinsics.checkNotNullParameter($this$ModalBottomSheet, "$this$ModalBottomSheet");
      ComposerKt.sourceInformation($composer, "C646@31535L17923:DetailComponents.kt#wxf82o");
      if ($composer.shouldExecute(($changed & 17) != 16, $changed & 1)) {
         if (ComposerKt.isTraceInProgress()) {
            ComposerKt.traceEventStart(-1693328034, $changed, -1, "com.android.snippets.ui.AddSnippetsModal.<anonymous> (DetailComponents.kt:646)");
         }

         Modifier var10000 = SizeKt.fillMaxWidth$default((Modifier)Modifier.Companion, 0.0F, 1, (Object)null);
         int var14 = 24;
         int var15 = 0;
         var10000 = PaddingKt.padding-VpY3zN4$default(var10000, Dp.constructor-impl((float)var14), 0.0F, 2, (Object)null);
         var14 = 24;
         var15 = 0;
         Modifier var120 = PaddingKt.padding-qDBjuR0$default(var10000, 0.0F, 0.0F, 0.0F, Dp.constructor-impl((float)var14), 7, (Object)null);
         Alignment.Horizontal var16 = Alignment.Companion.getCenterHorizontally();
         short var18 = 390;
         int var19 = 0;
         ComposerKt.sourceInformationMarkerStart($composer, 1341605231, "CC(Column)N(modifier,verticalArrangement,horizontalAlignment,content)87@4443L61,88@4509L134:Column.kt#2w3rfo");
         Arrangement.Vertical var122 = Arrangement.INSTANCE.getTop();
         MeasurePolicy var20 = ColumnKt.columnMeasurePolicy(var122, var16, $composer, 14 & var18 >> 3 | 112 & var18 >> 3);
         int var24 = 112 & var18 << 3;
         int var25 = 0;
         ComposerKt.sourceInformationMarkerStart($composer, -1159599143, "CC(Layout)N(content,modifier,measurePolicy)81@3355L27,84@3521L415:Layout.kt#80mrfh");
         int var26 = Long.hashCode(ComposablesKt.getCurrentCompositeKeyHashCode($composer, 0));
         CompositionLocalMap var27 = $composer.getCurrentCompositionLocalMap();
         Modifier var28 = ComposedModifierKt.materializeModifier($composer, var120);
         Function0 var29 = ComposeUiNode.Companion.getConstructor();
         int var31 = 6 | 896 & var24 << 6;
         int var32 = 0;
         ComposerKt.sourceInformationMarkerStart($composer, -553112988, "CC(ReusableComposeNode)N(factory,update,content)410@16187L9:Composables.kt#9igjgp");
         if (!($composer.getApplier() instanceof Applier)) {
            ComposablesKt.invalidApplier();
         }

         $composer.startReusableNode();
         if ($composer.getInserting()) {
            $composer.createNode(var29);
         } else {
            $composer.useNode();
         }

         Composer var33 = Updater.constructor-impl($composer);
         int var34 = 0;
         Updater.set-impl(var33, var20, ComposeUiNode.Companion.getSetMeasurePolicy());
         Updater.set-impl(var33, var27, ComposeUiNode.Companion.getSetResolvedCompositionLocals());
         Updater.set-impl(var33, var26, ComposeUiNode.Companion.getSetCompositeKeyHash());
         Updater.reconcile-impl(var33, ComposeUiNode.Companion.getApplyOnDeactivatedNodeAssertion());
         Updater.set-impl(var33, var28, ComposeUiNode.Companion.getSetModifier());
         int var35 = 14 & var31 >> 6;
         int var37 = 0;
         ComposerKt.sourceInformationMarkerStart($composer, 2093002350, "C89@4557L9:Column.kt#2w3rfo");
         int var38 = 6 | 112 & var18 >> 6;
         ColumnScope var40 = (ColumnScope)ColumnScopeInstance.INSTANCE;
         int var41 = 0;
         ComposerKt.sourceInformationMarkerStart($composer, 431936076, "C657@31943L21,653@31781L16493,893@48288L41,897@48427L11,899@48519L333,906@48933L515,895@48343L1105:DetailComponents.kt#wxf82o");
         Modifier var42 = ScrollKt.verticalScroll$default(var40.weight(SizeKt.fillMaxWidth$default((Modifier)Modifier.Companion, 0.0F, 1, (Object)null), 1.0F, false), ScrollKt.rememberScrollState(0, $composer, 0, 1), false, (FlingBehavior)null, false, 14, (Object)null);
         Alignment.Horizontal var43 = Alignment.Companion.getCenterHorizontally();
         short var45 = 384;
         int var46 = 0;
         ComposerKt.sourceInformationMarkerStart($composer, 1341605231, "CC(Column)N(modifier,verticalArrangement,horizontalAlignment,content)87@4443L61,88@4509L134:Column.kt#2w3rfo");
         Arrangement.Vertical var47 = Arrangement.INSTANCE.getTop();
         MeasurePolicy var48 = ColumnKt.columnMeasurePolicy(var47, var43, $composer, 14 & var45 >> 3 | 112 & var45 >> 3);
         int var52 = 112 & var45 << 3;
         int var53 = 0;
         ComposerKt.sourceInformationMarkerStart($composer, -1159599143, "CC(Layout)N(content,modifier,measurePolicy)81@3355L27,84@3521L415:Layout.kt#80mrfh");
         int var54 = Long.hashCode(ComposablesKt.getCurrentCompositeKeyHashCode($composer, 0));
         CompositionLocalMap var55 = $composer.getCurrentCompositionLocalMap();
         Modifier var56 = ComposedModifierKt.materializeModifier($composer, var42);
         Function0 var57 = ComposeUiNode.Companion.getConstructor();
         int var59 = 6 | 896 & var52 << 6;
         int var60 = 0;
         ComposerKt.sourceInformationMarkerStart($composer, -553112988, "CC(ReusableComposeNode)N(factory,update,content)410@16187L9:Composables.kt#9igjgp");
         if (!($composer.getApplier() instanceof Applier)) {
            ComposablesKt.invalidApplier();
         }

         $composer.startReusableNode();
         if ($composer.getInserting()) {
            $composer.createNode(var57);
         } else {
            $composer.useNode();
         }

         Composer var61 = Updater.constructor-impl($composer);
         int var62 = 0;
         Updater.set-impl(var61, var48, ComposeUiNode.Companion.getSetMeasurePolicy());
         Updater.set-impl(var61, var55, ComposeUiNode.Companion.getSetResolvedCompositionLocals());
         Updater.set-impl(var61, var54, ComposeUiNode.Companion.getSetCompositeKeyHash());
         Updater.reconcile-impl(var61, ComposeUiNode.Companion.getApplyOnDeactivatedNodeAssertion());
         Updater.set-impl(var61, var56, ComposeUiNode.Companion.getSetModifier());
         int var63 = 14 & var59 >> 6;
         int var65 = 0;
         ComposerKt.sourceInformationMarkerStart($composer, 2093002350, "C89@4557L9:Column.kt#2w3rfo");
         int var66 = 6 | 112 & var45 >> 6;
         ColumnScope var149 = (ColumnScope)ColumnScopeInstance.INSTANCE;
         int var69 = 0;
         ComposerKt.sourceInformationMarkerStart($composer, -1762148041, "C662@32158L10,664@32276L11,660@32066L314,672@32661L541,668@32398L804,686@33220L15040:DetailComponents.kt#wxf82o");
         TextStyle var70 = MaterialTheme.INSTANCE.getTypography($composer, MaterialTheme.$stable).getHeadlineSmall();
         FontWeight var71 = FontWeight.Companion.getBold();
         long var72 = MaterialTheme.INSTANCE.getColorScheme($composer, MaterialTheme.$stable).getOnSurface-0d7_KjU();
         Modifier var150 = (Modifier)Modifier.Companion;
         int var74 = 16;
         int var75 = 0;
         Modifier var76 = PaddingKt.padding-qDBjuR0$default(var150, 0.0F, 0.0F, 0.0F, Dp.constructor-impl((float)var74), 7, (Object)null);
         TextKt.Text-Nvy7gAk("Add a Snippet", var76, var72, (TextAutoSize)null, 0L, (FontStyle)null, var71, (FontFamily)null, 0L, (TextDecoration)null, (TextAlign)null, 0L, 0, false, 0, 0, (Function1)null, var70, $composer, 1572918, 0, 131000);
         var150 = SizeKt.fillMaxWidth$default((Modifier)Modifier.Companion, 0.0F, 1, (Object)null);
         int var136 = 24;
         int var77 = 0;
         Modifier var133 = PaddingKt.padding-qDBjuR0$default(var150, 0.0F, 0.0F, 0.0F, Dp.constructor-impl((float)var136), 7, (Object)null);
         Arrangement.HorizontalOrVertical var137 = Arrangement.INSTANCE.spacedBy-0680j_4(ButtonGroupDefaults.INSTANCE.getConnectedSpaceBetween-D9Ej5fM());
         Function3 var152 = com.android.snippets.ui.ComposableSingletons.DetailComponentsKt.INSTANCE.getLambda$2133249649$app_debug();
         Modifier var10001 = var133;
         float var10002 = 0.0F;
         Arrangement.Horizontal var10003 = (Arrangement.Horizontal)var137;
         ComposerKt.sourceInformationMarkerStart($composer, 1328633223, "CC(remember):DetailComponents.kt#9igjgp");
         boolean var144 = $composer.changedInstance($view);
         var74 = 0;
         Object var142 = $composer.rememberedValue();
         int var79 = 0;
         Object var10004;
         if (!var144 && var142 != Composer.Companion.getEmpty()) {
            var10004 = var142;
         } else {
            Arrangement.Horizontal var80 = var10003;
            float var81 = 0.0F;
            Function3 var83 = var152;
            int var84 = 0;
            Function1 var86_1 = DetailComponentsKt::AddSnippetsModal$lambda$158$lambda$157$lambda$152$lambda$112$lambda$111;
            var152 = var83;
            var10001 = var133;
            var10002 = var81;
            var10003 = var80;
            $composer.updateRememberedValue(var86_1);
            var10004 = var86_1;
         }

         Function1 var146 = (Function1)var10004;
         ComposerKt.sourceInformationMarkerEnd($composer);
         ButtonGroupKt.ButtonGroup(var152, var10001, var10002, var10003, var146, $composer, 54, 4);
         Modifier var153 = SizeKt.fillMaxWidth$default((Modifier)Modifier.Companion, 0.0F, 1, (Object)null);
         int var134 = 260;
         int var138 = 0;
         Modifier var135 = SizeKt.height-3ABfNKs(var153, Dp.constructor-impl((float)var134));
         var144 = (boolean)6;
         var74 = 0;
         ComposerKt.sourceInformationMarkerStart($composer, 1042775818, "CC(Box)N(modifier,contentAlignment,propagateMinConstraints,content)71@3424L131:Box.kt#2w3rfo");
         Alignment var139 = Alignment.Companion.getTopStart();
         boolean var147 = false;
         MeasurePolicy var143 = BoxKt.maybeCachedBoxMeasurePolicy(var139, var147);
         int var87 = 112 & var144 << 3;
         int var88 = 0;
         ComposerKt.sourceInformationMarkerStart($composer, -1159599143, "CC(Layout)N(content,modifier,measurePolicy)81@3355L27,84@3521L415:Layout.kt#80mrfh");
         int var89 = Long.hashCode(ComposablesKt.getCurrentCompositeKeyHashCode($composer, 0));
         CompositionLocalMap var90 = $composer.getCurrentCompositionLocalMap();
         Modifier var91 = ComposedModifierKt.materializeModifier($composer, var135);
         Function0 var92 = ComposeUiNode.Companion.getConstructor();
         int var94 = 6 | 896 & var87 << 6;
         int var95 = 0;
         ComposerKt.sourceInformationMarkerStart($composer, -553112988, "CC(ReusableComposeNode)N(factory,update,content)410@16187L9:Composables.kt#9igjgp");
         if (!($composer.getApplier() instanceof Applier)) {
            ComposablesKt.invalidApplier();
         }

         $composer.startReusableNode();
         if ($composer.getInserting()) {
            $composer.createNode(var92);
         } else {
            $composer.useNode();
         }

         Composer var96 = Updater.constructor-impl($composer);
         int var97 = 0;
         Updater.set-impl(var96, var143, ComposeUiNode.Companion.getSetMeasurePolicy());
         Updater.set-impl(var96, var90, ComposeUiNode.Companion.getSetResolvedCompositionLocals());
         Updater.set-impl(var96, var89, ComposeUiNode.Companion.getSetCompositeKeyHash());
         Updater.reconcile-impl(var96, ComposeUiNode.Companion.getApplyOnDeactivatedNodeAssertion());
         Updater.set-impl(var96, var91, ComposeUiNode.Companion.getSetModifier());
         int var98 = 14 & var94 >> 6;
         int var100 = 0;
         ComposerKt.sourceInformationMarkerStart($composer, 1833054614, "C72@3469L9:Box.kt#2w3rfo");
         int var101 = 6 | 112 & var144 >> 6;
         BoxScope var154 = (BoxScope)BoxScopeInstance.INSTANCE;
         int var104 = 0;
         ComposerKt.sourceInformationMarkerStart($composer, 714999832, "C689@33368L12,692@33539L272,698@33884L14358,690@33428L14814:DetailComponents.kt#wxf82o");
         MotionScheme var105 = MaterialTheme.INSTANCE.getMotionScheme($composer, MaterialTheme.$stable);
         Integer var155 = AddSnippetsModal$lambda$106($selectedIndex$delegate);
         Object var159 = null;
         ComposerKt.sourceInformationMarkerStart($composer, 438698400, "CC(remember):DetailComponents.kt#9igjgp");
         boolean var107 = $composer.changed(var105);
         int var108 = 0;
         Object var109 = $composer.rememberedValue();
         int var110 = 0;
         Object var160;
         if (!var107 && var109 != Composer.Companion.getEmpty()) {
            var160 = var109;
         } else {
            Object var111 = null;
            Integer var112 = var155;
            int var113 = 0;
            Function1 var115_1 = DetailComponentsKt::AddSnippetsModal$lambda$158$lambda$157$lambda$152$lambda$151$lambda$114$lambda$113;
            var155 = var112;
            var159 = var111;
            $composer.updateRememberedValue(var115_1);
            var160 = var115_1;
         }

         Function1 var116 = (Function1)var160;
         ComposerKt.sourceInformationMarkerEnd($composer);
         AnimatedContentKt.AnimatedContent(var155, (Modifier)var159, var116, (Alignment)null, "StepTransition", (Function1)null, (Function4)ComposableLambdaKt.rememberComposableLambda(-1041146982, true, DetailComponentsKt::AddSnippetsModal$lambda$158$lambda$157$lambda$152$lambda$151$lambda$150, $composer, 54), $composer, 1597440, 42);
         ComposerKt.sourceInformationMarkerEnd($composer);
         ComposerKt.sourceInformationMarkerEnd($composer);
         $composer.endNode();
         ComposerKt.sourceInformationMarkerEnd($composer);
         ComposerKt.sourceInformationMarkerEnd($composer);
         ComposerKt.sourceInformationMarkerEnd($composer);
         ComposerKt.sourceInformationMarkerEnd($composer);
         ComposerKt.sourceInformationMarkerEnd($composer);
         $composer.endNode();
         ComposerKt.sourceInformationMarkerEnd($composer);
         ComposerKt.sourceInformationMarkerEnd($composer);
         ComposerKt.sourceInformationMarkerEnd($composer);
         Modifier var156 = (Modifier)Modifier.Companion;
         int var123 = 16;
         int var128 = 0;
         SpacerKt.Spacer(SizeKt.height-3ABfNKs(var156, Dp.constructor-impl((float)var123)), $composer, 6);
         RoundedCornerShape var124 = RoundedCornerShapeKt.getCircleShape();
         long var117 = MaterialTheme.INSTANCE.getColorScheme($composer, MaterialTheme.$stable).getPrimary-0d7_KjU();
         boolean var44 = !StringsKt.isBlank((CharSequence)AddSnippetsModal$lambda$97($text$delegate));
         var156 = SizeKt.fillMaxWidth$default((Modifier)Modifier.Companion, 0.0F, 1, (Object)null);
         var46 = 56;
         int var129 = 0;
         Modifier var125 = SizeKt.height-3ABfNKs(var156, Dp.constructor-impl((float)var46));
         ComposerKt.sourceInformationMarkerStart($composer, 1399925025, "CC(remember):DetailComponents.kt#9igjgp");
         boolean var49 = $composer.changedInstance($view) | $composer.changedInstance($snippetColorsPalette) | $composer.changed($onAdd) | $composer.changed($onClose);
         int var50 = 0;
         Object var64_1 = $composer.rememberedValue();
         var52 = 0;
         Object var158;
         if (!var49 && var64_1 != Composer.Companion.getEmpty()) {
            var158 = var64_1;
         } else {
            var53 = 0;
            Object var132 = DetailComponentsKt::AddSnippetsModal$lambda$158$lambda$157$lambda$154$lambda$153;
            $composer.updateRememberedValue(var132);
            var158 = var132;
         }

         Function0 var127 = (Function0)var158;
         ComposerKt.sourceInformationMarkerEnd($composer);
         SurfaceKt.Surface-o_FOJdg(var127, var125, var44, (Shape)var124, var117, 0L, 0.0F, 0.0F, (BorderStroke)null, (MutableInteractionSource)null, (Function2)ComposableLambdaKt.rememberComposableLambda(841083423, true, DetailComponentsKt::AddSnippetsModal$lambda$158$lambda$157$lambda$156, $composer, 54), $composer, 48, 6, 992);
         ComposerKt.sourceInformationMarkerEnd($composer);
         ComposerKt.sourceInformationMarkerEnd($composer);
         $composer.endNode();
         ComposerKt.sourceInformationMarkerEnd($composer);
         ComposerKt.sourceInformationMarkerEnd($composer);
         ComposerKt.sourceInformationMarkerEnd($composer);
         if (ComposerKt.isTraceInProgress()) {
            ComposerKt.traceEventEnd();
         }
      } else {
         $composer.skipToGroupEnd();
      }

      return Unit.INSTANCE;
   }

   private static final Unit AddSnippetsModal$lambda$159(Photo $photo, Function3 $onAdd, Function0 $onClose, SnippetsViewModel $viewModel, int $$changed, Composer $composer, int $force) {
      AddSnippetsModal($photo, $onAdd, $onClose, $viewModel, $composer, RecomposeScopeImplKt.updateChangedFlags($$changed | 1));
      return Unit.INSTANCE;
   }

   private static final boolean CanvasBackgroundDialog$lambda$161(MutableState<Boolean> $isDarkSelected$delegate) {
      State var1 = (State)$isDarkSelected$delegate;
      Object var2 = null;
      KProperty var3 = null;
      int var4 = 0;
      return (Boolean)var1.getValue();
   }

   private static final void CanvasBackgroundDialog$lambda$162(MutableState<Boolean> $isDarkSelected$delegate, boolean var1) {
      Object var3 = null;
      Object var4 = null;
      Object var5 = var1;
      int var6 = 0;
      $isDarkSelected$delegate.setValue(var5);
   }

   private static final Unit CanvasBackgroundDialog$lambda$173$lambda$172$lambda$171$lambda$167$lambda$164$lambda$163(View $view, MutableState $isDarkSelected$delegate) {
      $view.performHapticFeedback(16);
      CanvasBackgroundDialog$lambda$162($isDarkSelected$delegate, false);
      return Unit.INSTANCE;
   }

   private static final Unit CanvasBackgroundDialog$lambda$173$lambda$172$lambda$171$lambda$167$lambda$166$lambda$165(View $view, MutableState $isDarkSelected$delegate) {
      $view.performHapticFeedback(16);
      CanvasBackgroundDialog$lambda$162($isDarkSelected$delegate, true);
      return Unit.INSTANCE;
   }

   private static final Unit CanvasBackgroundDialog$lambda$173$lambda$172$lambda$171$lambda$169$lambda$168(View $view, Function1 $onConfirm, MutableState $isDarkSelected$delegate) {
      $view.performHapticFeedback(16);
      $onConfirm.invoke(CanvasBackgroundDialog$lambda$161($isDarkSelected$delegate));
      return Unit.INSTANCE;
   }

   @Composable
   @ComposableTarget(
      applier = "androidx.compose.ui.UiComposable"
   )
   private static final Unit CanvasBackgroundDialog$lambda$173$lambda$172$lambda$171$lambda$170(ImageVector $buttonIcon, String $buttonText, RowScope $this$Button, Composer $composer, int $changed) {
      Intrinsics.checkNotNullParameter($this$Button, "$this$Button");
      ComposerKt.sourceInformation($composer, "C1013@53176L90,1014@53287L39,1015@53386L10,1015@53347L93:DetailComponents.kt#wxf82o");
      if ($composer.shouldExecute(($changed & 17) != 16, $changed & 1)) {
         if (ComposerKt.isTraceInProgress()) {
            ComposerKt.traceEventStart(-1818134512, $changed, -1, "com.android.snippets.ui.CanvasBackgroundDialog.<anonymous>.<anonymous>.<anonymous>.<anonymous> (DetailComponents.kt:1013)");
         }

         Modifier var10002 = (Modifier)Modifier.Companion;
         int var5 = 20;
         int var6 = 0;
         IconKt.Icon-ww6aTOc($buttonIcon, (String)null, SizeKt.size-3ABfNKs(var10002, Dp.constructor-impl((float)var5)), 0L, $composer, 432, 8);
         Modifier var10000 = (Modifier)Modifier.Companion;
         var5 = 8;
         var6 = 0;
         SpacerKt.Spacer(SizeKt.width-3ABfNKs(var10000, Dp.constructor-impl((float)var5)), $composer, 6);
         TextStyle var8 = MaterialTheme.INSTANCE.getTypography($composer, MaterialTheme.$stable).getLabelLarge();
         FontWeight var10 = FontWeight.Companion.getMedium();
         TextKt.Text-Nvy7gAk($buttonText, (Modifier)null, 0L, (TextAutoSize)null, 0L, (FontStyle)null, var10, (FontFamily)null, 0L, (TextDecoration)null, (TextAlign)null, 0L, 0, false, 0, 0, (Function1)null, var8, $composer, 1572864, 0, 131006);
         if (ComposerKt.isTraceInProgress()) {
            ComposerKt.traceEventEnd();
         }
      } else {
         $composer.skipToGroupEnd();
      }

      return Unit.INSTANCE;
   }

   @Composable
   @ComposableTarget(
      applier = "androidx.compose.ui.UiComposable"
   )
   private static final Unit CanvasBackgroundDialog$lambda$173$lambda$172(CanvasAction $action, View $view, Function1 $onConfirm, MutableState $isDarkSelected$delegate, Composer $composer, int $changed) {
      ComposerKt.sourceInformation($composer, "C942@50131L3341:DetailComponents.kt#wxf82o");
      if ($composer.shouldExecute(($changed & 3) != 2, $changed & 1)) {
         if (ComposerKt.isTraceInProgress()) {
            ComposerKt.traceEventStart(623457206, $changed, -1, "com.android.snippets.ui.CanvasBackgroundDialog.<anonymous>.<anonymous> (DetailComponents.kt:942)");
         }

         Modifier var10000 = (Modifier)Modifier.Companion;
         int var6 = 24;
         int var7 = 0;
         Modifier var76 = PaddingKt.padding-3ABfNKs(var10000, Dp.constructor-impl((float)var6));
         Alignment.Horizontal var8 = Alignment.Companion.getCenterHorizontally();
         short var10 = 390;
         int var11 = 0;
         ComposerKt.sourceInformationMarkerStart($composer, 1341605231, "CC(Column)N(modifier,verticalArrangement,horizontalAlignment,content)87@4443L61,88@4509L134:Column.kt#2w3rfo");
         Arrangement.Vertical var77 = Arrangement.INSTANCE.getTop();
         MeasurePolicy var12 = ColumnKt.columnMeasurePolicy(var77, var8, $composer, 14 & var10 >> 3 | 112 & var10 >> 3);
         int var16 = 112 & var10 << 3;
         int var17 = 0;
         ComposerKt.sourceInformationMarkerStart($composer, -1159599143, "CC(Layout)N(content,modifier,measurePolicy)81@3355L27,84@3521L415:Layout.kt#80mrfh");
         int var18 = Long.hashCode(ComposablesKt.getCurrentCompositeKeyHashCode($composer, 0));
         CompositionLocalMap var19 = $composer.getCurrentCompositionLocalMap();
         Modifier var20 = ComposedModifierKt.materializeModifier($composer, var76);
         Function0 var21 = ComposeUiNode.Companion.getConstructor();
         int var23 = 6 | 896 & var16 << 6;
         int var24 = 0;
         ComposerKt.sourceInformationMarkerStart($composer, -553112988, "CC(ReusableComposeNode)N(factory,update,content)410@16187L9:Composables.kt#9igjgp");
         if (!($composer.getApplier() instanceof Applier)) {
            ComposablesKt.invalidApplier();
         }

         $composer.startReusableNode();
         if ($composer.getInserting()) {
            $composer.createNode(var21);
         } else {
            $composer.useNode();
         }

         Composer var25 = Updater.constructor-impl($composer);
         int var26 = 0;
         Updater.set-impl(var25, var12, ComposeUiNode.Companion.getSetMeasurePolicy());
         Updater.set-impl(var25, var19, ComposeUiNode.Companion.getSetResolvedCompositionLocals());
         Updater.set-impl(var25, var18, ComposeUiNode.Companion.getSetCompositeKeyHash());
         Updater.reconcile-impl(var25, ComposeUiNode.Companion.getApplyOnDeactivatedNodeAssertion());
         Updater.set-impl(var25, var20, ComposeUiNode.Companion.getSetModifier());
         int var27 = 14 & var23 >> 6;
         int var29 = 0;
         ComposerKt.sourceInformationMarkerStart($composer, 2093002350, "C89@4557L9:Column.kt#2w3rfo");
         int var30 = 6 | 112 & var10 >> 6;
         ColumnScope var111 = (ColumnScope)ColumnScopeInstance.INSTANCE;
         int var33 = 0;
         ComposerKt.sourceInformationMarkerStart($composer, -350697786, "C950@50476L11,947@50325L241,954@50584L41,959@50766L10,960@50868L11,957@50668L354,966@51071L1172,995@52261L41,1002@52608L172,1009@53018L11,1010@53092L11,1008@52949L186,1012@53154L304,1001@52570L888:DetailComponents.kt#wxf82o");
         ImageVector var34 = PaletteKt.getPalette(Icons.INSTANCE.getDefault());
         long var35 = MaterialTheme.INSTANCE.getColorScheme($composer, MaterialTheme.$stable).getPrimary-0d7_KjU();
         Modifier var112 = (Modifier)Modifier.Companion;
         int var37 = 32;
         int var38 = 0;
         Modifier var39 = SizeKt.size-3ABfNKs(var112, Dp.constructor-impl((float)var37));
         IconKt.Icon-ww6aTOc(var34, (String)null, var39, var35, $composer, 432, 0);
         var112 = (Modifier)Modifier.Companion;
         int var78 = 16;
         int var40 = 0;
         SpacerKt.Spacer(SizeKt.height-3ABfNKs(var112, Dp.constructor-impl((float)var78)), $composer, 6);
         TextStyle var79 = TextStyle.copy-p1EtxEg$default(MaterialTheme.INSTANCE.getTypography($composer, MaterialTheme.$stable).getTitleLarge(), 0L, 0L, FontWeight.Companion.getNormal(), (FontStyle)null, (FontSynthesis)null, (FontFamily)null, (String)null, 0L, (BaselineShift)null, (TextGeometricTransform)null, (LocaleList)null, 0L, (TextDecoration)null, (Shadow)null, (DrawStyle)null, 0, 0, 0L, (TextIndent)null, (PlatformTextStyle)null, (LineHeightStyle)null, 0, 0, (TextMotion)null, 16777211, (Object)null);
         var35 = MaterialTheme.INSTANCE.getColorScheme($composer, MaterialTheme.$stable).getOnSurface-0d7_KjU();
         int var90 = TextAlign.Companion.getCenter-e0LSkKk();
         var112 = (Modifier)Modifier.Companion;
         var38 = 24;
         int var41 = 0;
         Modifier var84 = PaddingKt.padding-qDBjuR0$default(var112, 0.0F, 0.0F, 0.0F, Dp.constructor-impl((float)var38), 7, (Object)null);
         TextKt.Text-Nvy7gAk("Choose a background", var84, var35, (TextAutoSize)null, 0L, (FontStyle)null, (FontWeight)null, (FontFamily)null, 0L, (TextDecoration)null, TextAlign.box-impl(var90), 0L, 0, false, 0, 0, (Function1)null, var79, $composer, 54, 0, 130040);
         Modifier var80 = SizeKt.fillMaxWidth$default((Modifier)Modifier.Companion, 0.0F, 1, (Object)null);
         var40 = 16;
         int var42 = 0;
         Arrangement.Horizontal var93 = (Arrangement.Horizontal)Arrangement.INSTANCE.spacedBy-0680j_4(Dp.constructor-impl((float)var40));
         boolean var85 = (boolean)54;
         var38 = 0;
         ComposerKt.sourceInformationMarkerStart($composer, 844473419, "CC(Row)N(modifier,horizontalArrangement,verticalAlignment,content)99@5125L58,100@5188L131:Row.kt#2w3rfo");
         Alignment.Vertical var98 = Alignment.Companion.getTop();
         MeasurePolicy var96 = RowKt.rowMeasurePolicy(var93, var98, $composer, 14 & var85 >> 3 | 112 & var85 >> 3);
         int var46 = 112 & var85 << 3;
         int var47 = 0;
         ComposerKt.sourceInformationMarkerStart($composer, -1159599143, "CC(Layout)N(content,modifier,measurePolicy)81@3355L27,84@3521L415:Layout.kt#80mrfh");
         int var48 = Long.hashCode(ComposablesKt.getCurrentCompositeKeyHashCode($composer, 0));
         CompositionLocalMap var49 = $composer.getCurrentCompositionLocalMap();
         Modifier var50 = ComposedModifierKt.materializeModifier($composer, var80);
         Function0 var51 = ComposeUiNode.Companion.getConstructor();
         int var53 = 6 | 896 & var46 << 6;
         int var54 = 0;
         ComposerKt.sourceInformationMarkerStart($composer, -553112988, "CC(ReusableComposeNode)N(factory,update,content)410@16187L9:Composables.kt#9igjgp");
         if (!($composer.getApplier() instanceof Applier)) {
            ComposablesKt.invalidApplier();
         }

         $composer.startReusableNode();
         if ($composer.getInserting()) {
            $composer.createNode(var51);
         } else {
            $composer.useNode();
         }

         Composer var55 = Updater.constructor-impl($composer);
         int var56 = 0;
         Updater.set-impl(var55, var96, ComposeUiNode.Companion.getSetMeasurePolicy());
         Updater.set-impl(var55, var49, ComposeUiNode.Companion.getSetResolvedCompositionLocals());
         Updater.set-impl(var55, var48, ComposeUiNode.Companion.getSetCompositeKeyHash());
         Updater.reconcile-impl(var55, ComposeUiNode.Companion.getApplyOnDeactivatedNodeAssertion());
         Updater.set-impl(var55, var50, ComposeUiNode.Companion.getSetModifier());
         int var57 = 14 & var53 >> 6;
         int var59 = 0;
         ComposerKt.sourceInformationMarkerStart($composer, 1456264949, "C101@5233L9:Row.kt#2w3rfo");
         int var60 = 6 | 112 & var85 >> 6;
         RowScope var62 = (RowScope)RowScopeInstance.INSTANCE;
         int var63 = 0;
         ComposerKt.sourceInformationMarkerStart($composer, -1210959978, "C975@51467L181,971@51280L446,987@51967L180,983@51783L442:DetailComponents.kt#wxf82o");
         String var115 = "Light";
         boolean var10001 = false;
         boolean var10002 = !CanvasBackgroundDialog$lambda$161($isDarkSelected$delegate);
         ComposerKt.sourceInformationMarkerStart($composer, -1978720655, "CC(remember):DetailComponents.kt#9igjgp");
         boolean var65 = $composer.changedInstance($view);
         int var66 = 0;
         Object var67 = $composer.rememberedValue();
         int var68 = 0;
         Object var10003;
         if (!var65 && var67 != Composer.Companion.getEmpty()) {
            var10003 = var67;
         } else {
            boolean var69 = var10002;
            boolean var70 = false;
            String var71 = "Light";
            int var72 = 0;
            Function0 var74_1 = DetailComponentsKt::CanvasBackgroundDialog$lambda$173$lambda$172$lambda$171$lambda$167$lambda$164$lambda$163;
            var115 = var71;
            var10001 = var70;
            var10002 = var69;
            $composer.updateRememberedValue(var74_1);
            var10003 = var74_1;
         }

         Function0 var75 = (Function0)var10003;
         ComposerKt.sourceInformationMarkerEnd($composer);
         CanvasOptionCard(var115, var10001, var10002, var75, RowScope.weight$default(var62, (Modifier)Modifier.Companion, 1.0F, false, 2, (Object)null), $composer, 54, 0);
         var115 = "Dark";
         var10001 = true;
         var10002 = CanvasBackgroundDialog$lambda$161($isDarkSelected$delegate);
         ComposerKt.sourceInformationMarkerStart($composer, -1978704656, "CC(remember):DetailComponents.kt#9igjgp");
         var65 = $composer.changedInstance($view);
         var66 = 0;
         var67 = $composer.rememberedValue();
         var68 = 0;
         if (!var65 && var67 != Composer.Companion.getEmpty()) {
            var10003 = var67;
         } else {
            boolean var105 = var10002;
            boolean var106 = true;
            String var107 = "Dark";
            int var108 = 0;
            Function0 var74_1 = DetailComponentsKt::CanvasBackgroundDialog$lambda$173$lambda$172$lambda$171$lambda$167$lambda$166$lambda$165;
            var115 = var107;
            var10001 = var106;
            var10002 = var105;
            $composer.updateRememberedValue(var74_1);
            var10003 = var74_1;
         }

         var75 = (Function0)var10003;
         ComposerKt.sourceInformationMarkerEnd($composer);
         CanvasOptionCard(var115, var10001, var10002, var75, RowScope.weight$default(var62, (Modifier)Modifier.Companion, 1.0F, false, 2, (Object)null), $composer, 54, 0);
         ComposerKt.sourceInformationMarkerEnd($composer);
         ComposerKt.sourceInformationMarkerEnd($composer);
         $composer.endNode();
         ComposerKt.sourceInformationMarkerEnd($composer);
         ComposerKt.sourceInformationMarkerEnd($composer);
         ComposerKt.sourceInformationMarkerEnd($composer);
         Modifier var117 = (Modifier)Modifier.Companion;
         int var81 = 32;
         int var94 = 0;
         SpacerKt.Spacer(SizeKt.height-3ABfNKs(var117, Dp.constructor-impl((float)var81)), $composer, 6);
         String var82 = $action == CanvasAction.DOWNLOAD ? "Download" : "Share";
         ImageVector var95 = $action == CanvasAction.DOWNLOAD ? FileDownloadKt.getFileDownload(Icons.INSTANCE.getDefault()) : ShareKt.getShare(Icons.INSTANCE.getDefault());
         ComposerKt.sourceInformationMarkerStart($composer, 958588588, "CC(remember):DetailComponents.kt#9igjgp");
         var85 = $composer.changedInstance($view) | $composer.changed($onConfirm);
         var38 = 0;
         Object var97 = $composer.rememberedValue();
         int var43 = 0;
         Object var118;
         if (!var85 && var97 != Composer.Companion.getEmpty()) {
            var118 = var97;
         } else {
            int var44 = 0;
            Object var58_1 = DetailComponentsKt::CanvasBackgroundDialog$lambda$173$lambda$172$lambda$171$lambda$169$lambda$168;
            $composer.updateRememberedValue(var58_1);
            var118 = var58_1;
         }

         Function0 var99 = (Function0)var118;
         ComposerKt.sourceInformationMarkerEnd($composer);
         Function0 var119 = var99;
         Modifier var121 = SizeKt.fillMaxWidth$default((Modifier)Modifier.Companion, 0.0F, 1, (Object)null);
         int var100 = 48;
         var90 = 0;
         ButtonKt.Button(var119, SizeKt.height-3ABfNKs(var121, Dp.constructor-impl((float)var100)), false, (Shape)RoundedCornerShapeKt.RoundedCornerShape(100), ButtonDefaults.INSTANCE.buttonColors-ro_MJ88(MaterialTheme.INSTANCE.getColorScheme($composer, MaterialTheme.$stable).getPrimary-0d7_KjU(), MaterialTheme.INSTANCE.getColorScheme($composer, MaterialTheme.$stable).getOnPrimary-0d7_KjU(), 0L, 0L, $composer, ButtonDefaults.$stable << 12, 12), (ButtonElevation)null, (BorderStroke)null, (PaddingValues)null, (MutableInteractionSource)null, (Function3)ComposableLambdaKt.rememberComposableLambda(-1818134512, true, DetailComponentsKt::CanvasBackgroundDialog$lambda$173$lambda$172$lambda$171$lambda$170, $composer, 54), $composer, 805306416, 484);
         ComposerKt.sourceInformationMarkerEnd($composer);
         ComposerKt.sourceInformationMarkerEnd($composer);
         $composer.endNode();
         ComposerKt.sourceInformationMarkerEnd($composer);
         ComposerKt.sourceInformationMarkerEnd($composer);
         ComposerKt.sourceInformationMarkerEnd($composer);
         if (ComposerKt.isTraceInProgress()) {
            ComposerKt.traceEventEnd();
         }
      } else {
         $composer.skipToGroupEnd();
      }

      return Unit.INSTANCE;
   }

   @Composable
   @ComposableTarget(
      applier = "androidx.compose.ui.UiComposable"
   )
   private static final Unit CanvasBackgroundDialog$lambda$173(CanvasAction $action, View $view, Function1 $onConfirm, MutableState $isDarkSelected$delegate, Composer $composer, int $changed) {
      ComposerKt.sourceInformation($composer, "C938@49944L11,941@50117L3365,936@49854L3628:DetailComponents.kt#wxf82o");
      if ($composer.shouldExecute(($changed & 3) != 2, $changed & 1)) {
         if (ComposerKt.isTraceInProgress()) {
            ComposerKt.traceEventStart(-2008299493, $changed, -1, "com.android.snippets.ui.CanvasBackgroundDialog.<anonymous> (DetailComponents.kt:936)");
         }

         int var7 = 28;
         int var8 = 0;
         RoundedCornerShape var6 = RoundedCornerShapeKt.RoundedCornerShape-0680j_4(Dp.constructor-impl((float)var7));
         long var13 = MaterialTheme.INSTANCE.getColorScheme($composer, MaterialTheme.$stable).getSurfaceContainer-0d7_KjU();
         int var10 = 0;
         int var11 = 0;
         float var9 = Dp.constructor-impl((float)var10);
         Modifier var10000 = SizeKt.fillMaxWidth$default((Modifier)Modifier.Companion, 0.0F, 1, (Object)null);
         var11 = 24;
         int var12 = 0;
         var10000 = PaddingKt.padding-VpY3zN4$default(var10000, Dp.constructor-impl((float)var11), 0.0F, 2, (Object)null);
         var11 = 400;
         var12 = 0;
         SurfaceKt.Surface-T9BRK9s(SizeKt.widthIn-VpY3zN4$default(var10000, 0.0F, Dp.constructor-impl((float)var11), 1, (Object)null), (Shape)var6, var13, 0L, 0.0F, var9, (BorderStroke)null, (Function2)ComposableLambdaKt.rememberComposableLambda(623457206, true, DetailComponentsKt::CanvasBackgroundDialog$lambda$173$lambda$172, $composer, 54), $composer, 12779526, 88);
         if (ComposerKt.isTraceInProgress()) {
            ComposerKt.traceEventEnd();
         }
      } else {
         $composer.skipToGroupEnd();
      }

      return Unit.INSTANCE;
   }

   private static final Unit CanvasBackgroundDialog$lambda$174(CanvasAction $action, Function0 $onDismiss, Function1 $onConfirm, int $$changed, Composer $composer, int $force) {
      CanvasBackgroundDialog($action, $onDismiss, $onConfirm, $composer, RecomposeScopeImplKt.updateChangedFlags($$changed | 1));
      return Unit.INSTANCE;
   }

   @Composable
   @ComposableTarget(
      applier = "androidx.compose.ui.UiComposable"
   )
   private static final Unit CanvasOptionCard$lambda$178(boolean $isDark, boolean $isSelected, String $title, Composer $composer, int $changed) {
      ComposerKt.sourceInformation($composer, "C1041@54273L1790:DetailComponents.kt#wxf82o");
      if ($composer.shouldExecute(($changed & 3) != 2, $changed & 1)) {
         if (ComposerKt.isTraceInProgress()) {
            ComposerKt.traceEventStart(1213588215, $changed, -1, "com.android.snippets.ui.CanvasOptionCard.<anonymous> (DetailComponents.kt:1041)");
         }

         Modifier var10000 = (Modifier)Modifier.Companion;
         int var5 = 12;
         int var6 = 0;
         Modifier var72 = PaddingKt.padding-3ABfNKs(var10000, Dp.constructor-impl((float)var5));
         byte var9 = 6;
         int var10 = 0;
         ComposerKt.sourceInformationMarkerStart($composer, 1341605231, "CC(Column)N(modifier,verticalArrangement,horizontalAlignment,content)87@4443L61,88@4509L134:Column.kt#2w3rfo");
         Arrangement.Vertical var73 = Arrangement.INSTANCE.getTop();
         Alignment.Horizontal var7 = Alignment.Companion.getStart();
         MeasurePolicy var11 = ColumnKt.columnMeasurePolicy(var73, var7, $composer, 14 & var9 >> 3 | 112 & var9 >> 3);
         int var15 = 112 & var9 << 3;
         int var16 = 0;
         ComposerKt.sourceInformationMarkerStart($composer, -1159599143, "CC(Layout)N(content,modifier,measurePolicy)81@3355L27,84@3521L415:Layout.kt#80mrfh");
         int var17 = Long.hashCode(ComposablesKt.getCurrentCompositeKeyHashCode($composer, 0));
         CompositionLocalMap var18 = $composer.getCurrentCompositionLocalMap();
         Modifier var19 = ComposedModifierKt.materializeModifier($composer, var72);
         Function0 var20 = ComposeUiNode.Companion.getConstructor();
         int var22 = 6 | 896 & var15 << 6;
         int var23 = 0;
         ComposerKt.sourceInformationMarkerStart($composer, -553112988, "CC(ReusableComposeNode)N(factory,update,content)410@16187L9:Composables.kt#9igjgp");
         if (!($composer.getApplier() instanceof Applier)) {
            ComposablesKt.invalidApplier();
         }

         $composer.startReusableNode();
         if ($composer.getInserting()) {
            $composer.createNode(var20);
         } else {
            $composer.useNode();
         }

         Composer var24 = Updater.constructor-impl($composer);
         int var25 = 0;
         Updater.set-impl(var24, var11, ComposeUiNode.Companion.getSetMeasurePolicy());
         Updater.set-impl(var24, var18, ComposeUiNode.Companion.getSetResolvedCompositionLocals());
         Updater.set-impl(var24, var17, ComposeUiNode.Companion.getSetCompositeKeyHash());
         Updater.reconcile-impl(var24, ComposeUiNode.Companion.getApplyOnDeactivatedNodeAssertion());
         Updater.set-impl(var24, var19, ComposeUiNode.Companion.getSetModifier());
         int var26 = 14 & var22 >> 6;
         int var28 = 0;
         ComposerKt.sourceInformationMarkerStart($composer, 2093002350, "C89@4557L9:Column.kt#2w3rfo");
         int var29 = 6 | 112 & var9 >> 6;
         ColumnScope var100 = (ColumnScope)ColumnScopeInstance.INSTANCE;
         int var32 = 0;
         ComposerKt.sourceInformationMarkerStart($composer, 117867101, "C1044@54352L719,1062@55112L941:DetailComponents.kt#wxf82o");
         Alignment.Vertical var33 = Alignment.Companion.getCenterVertically();
         int var34 = 8;
         int var35 = 0;
         Arrangement.HorizontalOrVertical var36 = Arrangement.INSTANCE.spacedBy-0680j_4(Dp.constructor-impl((float)var34));
         Modifier var101 = (Modifier)Modifier.Companion;
         var35 = 12;
         int var37 = 0;
         Modifier var80 = PaddingKt.padding-qDBjuR0$default(var101, 0.0F, 0.0F, 0.0F, Dp.constructor-impl((float)var35), 7, (Object)null);
         Arrangement.Horizontal var85 = (Arrangement.Horizontal)var36;
         short var40 = 438;
         int var41 = 0;
         ComposerKt.sourceInformationMarkerStart($composer, 844473419, "CC(Row)N(modifier,horizontalArrangement,verticalAlignment,content)99@5125L58,100@5188L131:Row.kt#2w3rfo");
         MeasurePolicy var55_7 = RowKt.rowMeasurePolicy(var85, var33, $composer, 14 & var40 >> 3 | 112 & var40 >> 3);
         int var46 = 112 & var40 << 3;
         int var47 = 0;
         ComposerKt.sourceInformationMarkerStart($composer, -1159599143, "CC(Layout)N(content,modifier,measurePolicy)81@3355L27,84@3521L415:Layout.kt#80mrfh");
         int var48 = Long.hashCode(ComposablesKt.getCurrentCompositeKeyHashCode($composer, 0));
         CompositionLocalMap var49 = $composer.getCurrentCompositionLocalMap();
         Modifier var50 = ComposedModifierKt.materializeModifier($composer, var80);
         Function0 var51 = ComposeUiNode.Companion.getConstructor();
         int var53 = 6 | 896 & var46 << 6;
         int var54 = 0;
         ComposerKt.sourceInformationMarkerStart($composer, -553112988, "CC(ReusableComposeNode)N(factory,update,content)410@16187L9:Composables.kt#9igjgp");
         if (!($composer.getApplier() instanceof Applier)) {
            ComposablesKt.invalidApplier();
         }

         $composer.startReusableNode();
         if ($composer.getInserting()) {
            $composer.createNode(var51);
         } else {
            $composer.useNode();
         }

         Composer var55 = Updater.constructor-impl($composer);
         int var56 = 0;
         Updater.set-impl(var55, var55_7, ComposeUiNode.Companion.getSetMeasurePolicy());
         Updater.set-impl(var55, var49, ComposeUiNode.Companion.getSetResolvedCompositionLocals());
         Updater.set-impl(var55, var48, ComposeUiNode.Companion.getSetCompositeKeyHash());
         Updater.reconcile-impl(var55, ComposeUiNode.Companion.getApplyOnDeactivatedNodeAssertion());
         Updater.set-impl(var55, var50, ComposeUiNode.Companion.getSetModifier());
         int var57 = 14 & var53 >> 6;
         int var59 = 0;
         ComposerKt.sourceInformationMarkerStart($composer, 1456264949, "C101@5233L9:Row.kt#2w3rfo");
         int var60 = 6 | 112 & var40 >> 6;
         RowScope var102 = (RowScope)RowScopeInstance.INSTANCE;
         int var63 = 0;
         ComposerKt.sourceInformationMarkerStart($composer, -730479126, "C1049@54581L128,1055@54808L10,1053@54726L331:DetailComponents.kt#wxf82o");
         Modifier var10001 = (Modifier)Modifier.Companion;
         int var64 = 20;
         int var65 = 0;
         SelectionKt.CookieCheckmark($isSelected, SizeKt.size-3ABfNKs(var10001, Dp.constructor-impl((float)var64)), $composer, 48, 0);
         TextStyle var97 = MaterialTheme.INSTANCE.getTypography($composer, MaterialTheme.$stable).getLabelMedium();
         FontWeight var98 = $isSelected ? FontWeight.Companion.getBold() : FontWeight.Companion.getMedium();
         long var103;
         if ($isSelected) {
            $composer.startReplaceGroup(-162098524);
            ComposerKt.sourceInformation($composer, "1057@54979L11");
            long var66 = MaterialTheme.INSTANCE.getColorScheme($composer, MaterialTheme.$stable).getPrimary-0d7_KjU();
            $composer.endReplaceGroup();
            var103 = var66;
         } else {
            $composer.startReplaceGroup(-162097274);
            ComposerKt.sourceInformation($composer, "1057@55018L11");
            long var99 = MaterialTheme.INSTANCE.getColorScheme($composer, MaterialTheme.$stable).getOnSurface-0d7_KjU();
            $composer.endReplaceGroup();
            var103 = var99;
         }

         long var68 = var103;
         TextKt.Text-Nvy7gAk($title, (Modifier)null, var68, (TextAutoSize)null, 0L, (FontStyle)null, var98, (FontFamily)null, 0L, (TextDecoration)null, (TextAlign)null, 0L, 0, false, 0, 0, (Function1)null, var97, $composer, 0, 0, 131002);
         ComposerKt.sourceInformationMarkerEnd($composer);
         ComposerKt.sourceInformationMarkerEnd($composer);
         $composer.endNode();
         ComposerKt.sourceInformationMarkerEnd($composer);
         ComposerKt.sourceInformationMarkerEnd($composer);
         ComposerKt.sourceInformationMarkerEnd($composer);
         Modifier var104 = SizeKt.fillMaxSize$default((Modifier)Modifier.Companion, 0.0F, 1, (Object)null);
         long var108 = $isDark ? ColorKt.Color(4279374354L) : Color.Companion.getWhite-0d7_KjU();
         int var74 = 8;
         int var81 = 0;
         var104 = BackgroundKt.background-bw27NRU(var104, var108, (Shape)RoundedCornerShapeKt.RoundedCornerShape-0680j_4(Dp.constructor-impl((float)var74)));
         var74 = 1;
         var81 = 0;
         float var109 = Dp.constructor-impl((float)var74);
         long var10002;
         if ($isDark) {
            $composer.startReplaceGroup(1666404620);
            $composer.endReplaceGroup();
            var10002 = Color.Companion.getTransparent-0d7_KjU();
         } else {
            $composer.startReplaceGroup(1666406483);
            ComposerKt.sourceInformation($composer, "1071@55513L11");
            long var70 = Color.copy-wmQWz5c$default(MaterialTheme.INSTANCE.getColorScheme($composer, MaterialTheme.$stable).getOutlineVariant-0d7_KjU(), 0.5F, 0.0F, 0.0F, 0.0F, 14, (Object)null);
            $composer.endReplaceGroup();
            var10002 = var70;
         }

         var74 = 8;
         var81 = 0;
         Modifier var77 = BorderKt.border-xT4_qwU(var104, var109, var10002, (Shape)RoundedCornerShapeKt.RoundedCornerShape-0680j_4(Dp.constructor-impl((float)var74)));
         Alignment var84 = Alignment.Companion.getCenter();
         byte var86 = 48;
         int var38 = 0;
         ComposerKt.sourceInformationMarkerStart($composer, 1042775818, "CC(Box)N(modifier,contentAlignment,propagateMinConstraints,content)71@3424L131:Box.kt#2w3rfo");
         var34 = 0;
         MeasurePolicy var39 = BoxKt.maybeCachedBoxMeasurePolicy(var84, (boolean)var34);
         int var43 = 112 & var86 << 3;
         int var44 = 0;
         ComposerKt.sourceInformationMarkerStart($composer, -1159599143, "CC(Layout)N(content,modifier,measurePolicy)81@3355L27,84@3521L415:Layout.kt#80mrfh");
         int var58_1 = Long.hashCode(ComposablesKt.getCurrentCompositeKeyHashCode($composer, 0));
         CompositionLocalMap var87 = $composer.getCurrentCompositionLocalMap();
         Modifier var88 = ComposedModifierKt.materializeModifier($composer, var77);
         Function0 var89 = ComposeUiNode.Companion.getConstructor();
         int var90 = 6 | 896 & var43 << 6;
         int var91 = 0;
         ComposerKt.sourceInformationMarkerStart($composer, -553112988, "CC(ReusableComposeNode)N(factory,update,content)410@16187L9:Composables.kt#9igjgp");
         if (!($composer.getApplier() instanceof Applier)) {
            ComposablesKt.invalidApplier();
         }

         $composer.startReusableNode();
         if ($composer.getInserting()) {
            $composer.createNode(var89);
         } else {
            $composer.useNode();
         }

         Composer var52 = Updater.constructor-impl($composer);
         var53 = 0;
         Updater.set-impl(var52, var39, ComposeUiNode.Companion.getSetMeasurePolicy());
         Updater.set-impl(var52, var87, ComposeUiNode.Companion.getSetResolvedCompositionLocals());
         Updater.set-impl(var52, var58_1, ComposeUiNode.Companion.getSetCompositeKeyHash());
         Updater.reconcile-impl(var52, ComposeUiNode.Companion.getApplyOnDeactivatedNodeAssertion());
         Updater.set-impl(var52, var88, ComposeUiNode.Companion.getSetModifier());
         var54 = 14 & var90 >> 6;
         var56 = 0;
         ComposerKt.sourceInformationMarkerStart($composer, 1833054614, "C72@3469L9:Box.kt#2w3rfo");
         var57 = 6 | 112 & var86 >> 6;
         BoxScope var106 = (BoxScope)BoxScopeInstance.INSTANCE;
         var60 = 0;
         ComposerKt.sourceInformationMarkerStart($composer, -1961978228, "C1077@55755L284:DetailComponents.kt#wxf82o");
         ImageVector var107 = PhotoKt.getPhoto(Icons.INSTANCE.getDefault());
         Modifier var110 = (Modifier)Modifier.Companion;
         int var61 = 32;
         int var62 = 0;
         IconKt.Icon-ww6aTOc(var107, (String)null, SizeKt.size-3ABfNKs(var110, Dp.constructor-impl((float)var61)), $isDark ? Color.copy-wmQWz5c$default(Color.Companion.getWhite-0d7_KjU(), 0.2F, 0.0F, 0.0F, 0.0F, 14, (Object)null) : Color.copy-wmQWz5c$default(Color.Companion.getBlack-0d7_KjU(), 0.1F, 0.0F, 0.0F, 0.0F, 14, (Object)null), $composer, 432, 0);
         ComposerKt.sourceInformationMarkerEnd($composer);
         ComposerKt.sourceInformationMarkerEnd($composer);
         $composer.endNode();
         ComposerKt.sourceInformationMarkerEnd($composer);
         ComposerKt.sourceInformationMarkerEnd($composer);
         ComposerKt.sourceInformationMarkerEnd($composer);
         ComposerKt.sourceInformationMarkerEnd($composer);
         ComposerKt.sourceInformationMarkerEnd($composer);
         $composer.endNode();
         ComposerKt.sourceInformationMarkerEnd($composer);
         ComposerKt.sourceInformationMarkerEnd($composer);
         ComposerKt.sourceInformationMarkerEnd($composer);
         if (ComposerKt.isTraceInProgress()) {
            ComposerKt.traceEventEnd();
         }
      } else {
         $composer.skipToGroupEnd();
      }

      return Unit.INSTANCE;
   }

   private static final Unit CanvasOptionCard$lambda$179(String $title, boolean $isDark, boolean $isSelected, Function0 $onClick, Modifier $modifier, int $$changed, int $$default, Composer $composer, int $force) {
      CanvasOptionCard($title, $isDark, $isSelected, $onClick, $modifier, $composer, RecomposeScopeImplKt.updateChangedFlags($$changed | 1), $$default);
      return Unit.INSTANCE;
   }

   // $FF: synthetic method
   public static final void access$CurrentSnippetsModal$lambda$94$lambda$71(MutableState $animateScale$delegate, float var1) {
      CurrentSnippetsModal$lambda$94$lambda$71($animateScale$delegate, var1);
   }

   // $FF: synthetic method
   public static final void access$AddSnippetsModal$lambda$101(MutableState $selectedColor$delegate, Integer var1) {
      AddSnippetsModal$lambda$101($selectedColor$delegate, var1);
   }

   // $FF: synthetic method
   public static final Integer access$AddSnippetsModal$lambda$100(MutableState $selectedColor$delegate) {
      return AddSnippetsModal$lambda$100($selectedColor$delegate);
   }
}
