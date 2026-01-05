<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;
use Illuminate\Support\Facades\Hash;

class Client extends Model
{
    use HasFactory;

    protected $fillable = [
        'nom',
        'email',
        'telephone',
        'adresse',
        'password',
    ];

    protected $hidden = [
        'password',
    ];

    /**
     * Mutator pour hacher automatiquement le mot de passe
     */
    public function setPasswordAttribute($value)
    {
        $this->attributes['password'] = Hash::make($value);
    }

    public function locations()
    {
        return $this->hasMany(Location::class);
    }
}

