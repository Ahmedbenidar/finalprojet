<?php

namespace App\Http\Controllers;

use App\Models\Client;
use Illuminate\Http\Request;

class ClientController extends Controller
{
    public function index()
    {
        return response()->json(Client::all());
    }

    public function store(Request $request)
    {
        $data = $request->validate([
            'nom' => ['required', 'string', 'max:255'],
            'email' => ['required', 'email', 'max:320', 'unique:clients,email'],
            'telephone' => ['nullable', 'string', 'max:50'],
            'adresse' => ['nullable', 'string', 'max:255'],
            'password' => ['required', 'string', 'min:8'],
        ]);

        $client = Client::create($data);

        return response()->json($client, 201);
    }

    public function show(Client $client)
    {
        return response()->json($client);
    }

    public function update(Request $request, Client $client)
    {
        $data = $request->validate([
            'nom' => ['sometimes', 'string', 'max:255'],
            'email' => ['sometimes', 'email', 'max:320', 'unique:clients,email,' . $client->id],
            'telephone' => ['nullable', 'string', 'max:50'],
            'adresse' => ['nullable', 'string', 'max:255'],
            'password' => ['sometimes', 'string', 'min:8'],
        ]);

        $client->update($data);

        return response()->json($client);
    }

    public function destroy(Client $client)
    {
        $client->delete();

        return response()->noContent();
    }
}

