<?php

namespace App\Http\Controllers;

use App\Models\Location;
use Illuminate\Http\Request;

class LocationController extends Controller
{
    public function index()
    {
        return response()->json(Location::with(['client', 'costume'])->get());
    }

    public function store(Request $request)
    {
        $data = $request->validate([
            'date_debut' => ['required', 'date'],
            'date_fin' => ['required', 'date', 'after_or_equal:date_debut'],
            'client_id' => ['required', 'exists:clients,id'],
            'costume_id' => ['required', 'exists:costumes,id'],
            'prix_total' => ['nullable', 'numeric', 'min:0'],
        ]);

        $location = Location::create($data);

        return response()->json($location->load(['client', 'costume']), 201);
    }

    public function show(Location $location)
    {
        return response()->json($location->load(['client', 'costume']));
    }

    public function update(Request $request, Location $location)
    {
        $data = $request->validate([
            'date_debut' => ['sometimes', 'date'],
            'date_fin' => ['sometimes', 'date', 'after_or_equal:date_debut'],
            'client_id' => ['sometimes', 'exists:clients,id'],
            'costume_id' => ['sometimes', 'exists:costumes,id'],
            'prix_total' => ['nullable', 'numeric', 'min:0'],
        ]);

        $location->update($data);

        return response()->json($location->load(['client', 'costume']));
    }

    public function destroy(Location $location)
    {
        $location->delete();

        return response()->noContent();
    }
}

