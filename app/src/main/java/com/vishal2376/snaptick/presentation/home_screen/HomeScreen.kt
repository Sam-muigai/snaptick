package com.vishal2376.snaptick.presentation.home_screen

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vishal2376.snaptick.R
import com.vishal2376.snaptick.domain.model.Task
import com.vishal2376.snaptick.presentation.common.SnackbarController.showCustomSnackbar
import com.vishal2376.snaptick.presentation.common.SortTask
import com.vishal2376.snaptick.presentation.common.SwipeActionBox
import com.vishal2376.snaptick.presentation.common.fontRobotoMono
import com.vishal2376.snaptick.presentation.common.h1TextStyle
import com.vishal2376.snaptick.presentation.common.h2TextStyle
import com.vishal2376.snaptick.presentation.home_screen.components.EmptyTaskComponent
import com.vishal2376.snaptick.presentation.home_screen.components.InfoComponent
import com.vishal2376.snaptick.presentation.home_screen.components.NavigationDrawerComponent
import com.vishal2376.snaptick.presentation.home_screen.components.SortTaskDialogComponent
import com.vishal2376.snaptick.presentation.home_screen.components.TaskComponent
import com.vishal2376.snaptick.presentation.main.MainEvent
import com.vishal2376.snaptick.presentation.main.MainState
import com.vishal2376.snaptick.ui.theme.Blue
import com.vishal2376.snaptick.ui.theme.Green
import com.vishal2376.snaptick.ui.theme.SnaptickTheme
import com.vishal2376.snaptick.ui.theme.Yellow
import com.vishal2376.snaptick.util.Constants
import com.vishal2376.snaptick.util.DummyTasks
import com.vishal2376.snaptick.util.getFreeTime
import kotlinx.coroutines.launch


@OptIn(
	ExperimentalMaterial3Api::class,
	ExperimentalFoundationApi::class
)
@Composable
fun HomeScreen(
	tasks: List<Task>,
	appState: MainState,
	onMainEvent: (MainEvent) -> Unit,
	onEvent: (HomeScreenEvent) -> Unit,
	onEditTask: (id: Int) -> Unit,
	onAddTask: () -> Unit,
	onClickCompletedInfo: () -> Unit,
	onClickFreeTimeInfo: () -> Unit,
	onPomodoroTask: (id: Int) -> Unit,
) {


	val completedTasks = mutableListOf<Task>()
	val inCompletedTasks = mutableListOf<Task>()

	tasks.filterTo(completedTasks) { it.isCompleted }
	tasks.filterTo(inCompletedTasks) { !it.isCompleted }

	// calc free time
	val totalTaskTime = inCompletedTasks.sumOf { it.getDuration(checkPastTask = true) }
	val freeTimeText = getFreeTime(totalTaskTime)

	LaunchedEffect(inCompletedTasks) {
		appState.totalTaskDuration = totalTaskTime
	}

	val totalTasks = tasks.size
	val totalCompletedTasks = completedTasks.size
	val context = LocalContext.current

	// animation
	val translateX = 600f
	val leftTranslate = remember { Animatable(-translateX) }
	val rightTranslate = remember { Animatable(translateX) }

	LaunchedEffect(key1 = Unit) {
		launch {
			leftTranslate.animateTo(
				0f,
				tween(1000)
			)
		}
		launch {
			rightTranslate.animateTo(
				0f,
				tween(1000)
			)
		}
	}

	//sort dialog
	var showSortDialog by remember {
		mutableStateOf(false)
	}

	// navigation drawer
	val scope = rememberCoroutineScope()
	val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

	ModalNavigationDrawer(
		drawerState = drawerState,
		drawerContent = {
			ModalDrawerSheet(drawerContainerColor = MaterialTheme.colorScheme.primary) {
				NavigationDrawerComponent(appState.theme, appState.buildVersion, onMainEvent)
			}
		}) {
		Scaffold(topBar = {
			TopAppBar(
				colors = TopAppBarDefaults.topAppBarColors(
					containerColor = Color.Transparent
				),
				title = {
					Text(
						text = stringResource(id = R.string.app_name),
						style = h1TextStyle
					)
				},
				navigationIcon = {
					IconButton(onClick = {
						scope.launch {
							drawerState.apply {
								if (isClosed) open() else close()
							}
						}
					}) {
						Icon(
							imageVector = Icons.Default.Menu,
							contentDescription = null
						)
					}
				},
				actions = {
					Text(
						text = appState.streak.toString(),
						fontSize = 18.sp,
						fontFamily = fontRobotoMono,
						fontWeight = FontWeight.Bold
					)
					Spacer(modifier = Modifier.width(4.dp))
					Icon(
						painter = painterResource(id = R.drawable.ic_fire),
						contentDescription = null,
						tint = Yellow,
						modifier = Modifier.size(22.dp)
					)
				})
		},
			floatingActionButton = {
				FloatingActionButton(
					onClick = {
						onAddTask()
					},
					containerColor = Blue,
					contentColor = MaterialTheme.colorScheme.secondary
				) {
					Icon(
						imageVector = Icons.Default.Add,
						contentDescription = null
					)
				}
			}) { innerPadding ->

			// sort dialog
			if (showSortDialog)
				SortTaskDialogComponent(
					defaultSortTask = appState.sortBy,
					onClose = { showSortDialog = false },
					onSelect = {
						onMainEvent(MainEvent.UpdateSortByTask(it, context = context))
						showSortDialog = false
					}
				)

			Column(modifier = Modifier.padding(innerPadding)) {
				Row(
					modifier = Modifier
						.fillMaxWidth()
						.padding(
							16.dp,
							8.dp
						),
					horizontalArrangement = Arrangement.spacedBy(16.dp),
					verticalAlignment = Alignment.CenterVertically
				) {

					InfoComponent(
						title = stringResource(R.string.completed),
						desc = "$totalCompletedTasks/$totalTasks Tasks",
						icon = R.drawable.ic_task_list,
						backgroundColor = Green,
						modifier = Modifier
							.weight(1f)
							.graphicsLayer {
								translationX = leftTranslate.value
							},
						onClick = { onClickCompletedInfo() }
					)

					InfoComponent(
						title = stringResource(R.string.free_time),
						desc = freeTimeText,
						icon = R.drawable.ic_clock,
						backgroundColor = Blue,
						modifier = Modifier
							.weight(1f)
							.graphicsLayer {
								translationX = rightTranslate.value
							},
						onClick = {
							if (inCompletedTasks.isEmpty()) {
//								CustomSnackBar("Add Task to analyze", 1000)
								showCustomSnackbar("Add Tasks to Analyze")
							} else {
								onClickFreeTimeInfo()
							}
						}
					)

				}

				if (inCompletedTasks.isEmpty()) {
					EmptyTaskComponent()
				} else {

					Row(
						modifier = Modifier
							.fillMaxWidth()
							.padding(horizontal = 8.dp),
						horizontalArrangement = Arrangement.SpaceBetween,
						verticalAlignment = Alignment.CenterVertically
					) {

						Text(
							text = stringResource(R.string.today_tasks),
							style = h2TextStyle,
							color = Color.White,
							modifier = Modifier.padding(16.dp)
						)

						IconButton(onClick = { showSortDialog = true }) {
							Icon(
								imageVector = Icons.Default.Sort,
								contentDescription = null,
								tint = Color.White
							)
						}

					}

					// sort task list
					val sortedTasks: List<Task> = remember(inCompletedTasks, appState.sortBy) {
						inCompletedTasks.sortedWith(compareBy {
							when (appState.sortBy) {
								SortTask.BY_CREATE_TIME_ASCENDING -> {
									it.id
								}

								SortTask.BY_CREATE_TIME_DESCENDING -> {
									-it.id
								}

								SortTask.BY_PRIORITY_ASCENDING -> {
									it.priority
								}

								SortTask.BY_PRIORITY_DESCENDING -> {
									-it.priority
								}

								SortTask.BY_START_TIME_ASCENDING -> {
									it.startTime.toSecondOfDay()
								}

								SortTask.BY_START_TIME_DESCENDING -> {
									-it.startTime.toSecondOfDay()
								}
							}
						})
					}

					LazyColumn(
						modifier = Modifier
							.fillMaxSize()
							.padding(
								16.dp,
								0.dp
							)
					) {

						itemsIndexed(items = sortedTasks,
							key = { _, task ->
								task.id
							}) { index, task ->
							Box(
								modifier = Modifier.animateItemPlacement(tween(500))
							) {
								SwipeActionBox(item = task, onAction = {
									onEvent(HomeScreenEvent.OnSwipeTask(it))
								})
								{
									TaskComponent(
										task = task,
										onEdit = {
											onEvent(HomeScreenEvent.OnEditTask(it))
											onEditTask(it)
										},
										onComplete = {
											onEvent(HomeScreenEvent.OnCompleted(it, true))
										},
										onPomodoro = {
											onEvent(HomeScreenEvent.OnPomodoro(it))
											onPomodoroTask(it)
										},
										animDelay = index * Constants.LIST_ANIMATION_DELAY
									)
								}
							}
							Spacer(modifier = Modifier.height(10.dp))
						}
					}
				}
			}
		}
	}
}

@Preview
@Composable
fun HomeScreenPreview() {
	SnaptickTheme {
		val tasks = DummyTasks.tasks
		HomeScreen(tasks = tasks, MainState(), {}, {}, {}, {}, {}, {}, {})
	}
}