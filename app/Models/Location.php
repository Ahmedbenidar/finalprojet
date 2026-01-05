<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;

class Location extends Model
{
    use HasFactory;

    protected $fillable = [
        'date_debut',
        'date_fin',
        'client_id',
        'costume_id',
        'prix_total',
    ];

    protected $casts = [
        'date_debut' => 'date',
        'date_fin' => 'date',
    ];

    public function client()
    {
        return $this->belongsTo(Client::class);
    }

    public function costume()
    {
        return $this->belongsTo(Costume::class);
    }
}

