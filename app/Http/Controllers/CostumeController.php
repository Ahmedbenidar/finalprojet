<?php

namespace App\Http\Controllers;

use App\Models\Costume;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Storage;

class CostumeController extends Controller
{
    public function index()
    {
        return response()->json(Costume::all());
    }

    public function store(Request $request)
    {
        $data = $request->validate([
            'nom' => ['required', 'string', 'max:255'],
            'type' => ['required', 'string', 'max:255'],
            'taille' => ['required', 'string', 'max:255'],
            'prix' => ['required', 'numeric', 'min:0'],
            'image_path' => ['nullable', 'string', 'max:500'],
            'disponibilite' => ['boolean'],
        ]);

        $costume = Costume::create($data);

        return response()->json($costume, 201);
    }

    public function show(Costume $costume)
    {
        return response()->json($costume);
    }

    public function update(Request $request, Costume $costume)
    {
        $data = $request->validate([
            'nom' => ['sometimes', 'string', 'max:255'],
            'type' => ['sometimes', 'string', 'max:255'],
            'taille' => ['sometimes', 'string', 'max:255'],
            'prix' => ['sometimes', 'numeric', 'min:0'],
            'image_path' => ['nullable', 'string', 'max:500'],
            'disponibilite' => ['boolean'],
        ]);

        $costume->update($data);

        return response()->json($costume);
    }

    public function destroy(Costume $costume)
    {
        $costume->delete();

        return response()->noContent();
    }

    public function getImage(Costume $costume)
    {
        if (! $costume->image_path || ! Storage::exists($costume->image_path)) {
            return response()->json(['message' => 'Image non trouvée'], 404);
        }

        return Storage::download($costume->image_path);
    }
}

