<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    /**
     * Run the migrations.
     */
    public function up(): void
    {
        Schema::create('locations', function (Blueprint $table) {
            $table->id();
            $table->date('date_debut');
            $table->date('date_fin');
            $table->foreignId('client_id')->constrained('clients');
            $table->foreignId('costume_id')->constrained('costumes');
            $table->decimal('prix_total', 10, 2)->nullable();
            $table->timestamps();
            $table->index(['costume_id', 'date_debut', 'date_fin'], 'locations_costume_date_index');
        });
    }

    /**
     * Reverse the migrations.
     */
    public function down(): void
    {
        Schema::dropIfExists('locations');
    }
};

