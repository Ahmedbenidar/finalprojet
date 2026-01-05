<?php

use App\Http\Controllers\ClientController;
use App\Http\Controllers\CostumeController;
use App\Http\Controllers\LocationController;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Route;

/*
|--------------------------------------------------------------------------
| API Routes
|--------------------------------------------------------------------------
|
| Endpoints JSON pour l'app mobile. On expose les CRUD et quelques routes
| dédiées (ex: image de costume).
|
*/

Route::get('/', function () {
    return response()->json(['message' => 'API costumes OK']);
});

Route::middleware('auth:sanctum')->get('/user', function (Request $request) {
    return $request->user();
});

// Costumes
Route::controller(CostumeController::class)->group(function () {
    Route::get('/costumes', 'index');
    Route::get('/costumes/{costume}', 'show');
    Route::post('/costumes', 'store');
    Route::put('/costumes/{costume}', 'update');
    Route::delete('/costumes/{costume}', 'destroy');
    Route::get('/costumes/{costume}/image', 'getImage');
});

// Clients
Route::controller(ClientController::class)->group(function () {
    Route::get('/clients', 'index');
    Route::get('/clients/{client}', 'show');
    Route::post('/clients', 'store');
    Route::put('/clients/{client}', 'update');
    Route::delete('/clients/{client}', 'destroy');
});

// Locations (réservations)
Route::controller(LocationController::class)->group(function () {
    Route::get('/locations', 'index');
    Route::get('/locations/{location}', 'show');
    Route::post('/locations', 'store');
    Route::put('/locations/{location}', 'update');
    Route::delete('/locations/{location}', 'destroy');
});

