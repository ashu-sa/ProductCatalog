package com.spirelab.productcatalog.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.spirelab.productcatalog.di.AppContainer
import com.spirelab.productcatalog.ui.cart.CartScreen
import com.spirelab.productcatalog.ui.cart.CartViewModel
import com.spirelab.productcatalog.ui.productdetail.ProductDetailScreen
import com.spirelab.productcatalog.ui.productdetail.ProductDetailViewModel
import com.spirelab.productcatalog.ui.productlist.ProductListScreen
import com.spirelab.productcatalog.ui.productlist.ProductListViewModel
import com.spirelab.productcatalog.util.observeConnectivity
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

object Routes {
    const val LIST = "list"
    const val DETAIL = "detail/{productId}"
    const val CART = "cart"
    fun detail(id: Int) = "detail/$id"
}

@Composable
fun CatalogNavHost(container: AppContainer) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val isConnected by remember(container) {
        observeConnectivity(context.applicationContext)
    }.collectAsState(initial = true)
    val isOffline = !isConnected

    // Shared cart state for badge on listing screen
    val cartVm: CartViewModel = viewModel(factory = CartViewModel.Factory(container.cartRepository))
    val summary by cartVm.summary.collectAsState()

    NavHost(navController = navController, startDestination = Routes.LIST) {
        composable(Routes.LIST) {
            val listVm: ProductListViewModel =
                viewModel(factory = ProductListViewModel.Factory(container.productRepository))
            androidx.compose.runtime.LaunchedEffect(isOffline) {
                listVm.setOfflineBanner(isOffline)
            }
            ProductListScreen(
                viewModel = listVm,
                cartCount = summary.itemCount,
                isOffline = isOffline,
                onProductClick = { navController.navigate(Routes.detail(it.id)) },
                onCartClick = { navController.navigate(Routes.CART) }
            )
        }
        composable(
            Routes.DETAIL,
            arguments = listOf(navArgument("productId") { type = NavType.IntType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getInt("productId") ?: 0
            val detailVm: ProductDetailViewModel = viewModel(
                key = "detail_$id",
                factory = ProductDetailViewModel.Factory(id, container.productRepository, container.cartRepository)
            )
            ProductDetailScreen(
                viewModel = detailVm,
                onBack = { navController.popBackStack() },
                onCartClick = { navController.navigate(Routes.CART) }
            )
        }
        composable(Routes.CART) {
            CartScreen(
                viewModel = cartVm,
                isOffline = isOffline,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
